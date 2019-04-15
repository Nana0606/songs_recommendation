# python3
# -*- coding: utf-8 -*-
# @Author  : lina
# @Time    : 2019-04-13 13:21

import pymysql
from redis import Redis
import numpy as np

"""
在推荐系统中，如果同时输入singer name和song name，则需要判断song name是歌曲名还是关键词，这时候需要获取歌曲对应的歌手名，
所以这里引入倒排索引。
"""


def read_data(connection):
    """
    读取mysql中song的数据
    :param connection: mysql数据连接
    :return: song_singers
    """
    song_singers = {}   # dict，key是song name, value是所有singer name
    sql_select = "SELECT LOWER(song_name), GROUP_CONCAT(singer_name) as singers FROM songs GROUP BY song_name"
    cursor = connection.cursor()
    cursor.execute(sql_select)
    results = cursor.fetchall()
    for result in results:
        song_singers[result[0]] = "\"" + result[1] + "\""
    return song_singers


def save_to_redis(song_singers):
    """
    将song_singers的结果存入redis，在redis中的key是singers
    :param song_singers:
    :return:
    """
    redis = Redis(host='localhost', port=6379, db=0, decode_responses=True)
    for key, value in song_singers.items():
        redis.hset(key, "singers", value)


def cal_cosine_sim(vec1, vec2):
    """
    计算vector1和vector2的余弦相似度
    :param vec1:
    :param vec2:
    :return:
    """
    num = np.sum(vec1 * vec2.T)
    print(num)
    denom = np.linalg.norm(vec1) * np.linalg.norm(vec2)
    cos = num / denom
    return cos


def deduplicate_data(connection):
    """
    歌曲去重处理，并更新Mysql数据库。
    :param connection:
    :return:
    """
    songs = {}
    song_ids = []

    # 读取数据
    sql_select = "SELECT song_name, singer_name, vector, song_id, comments_num FROM songs"
    cursor = connection.cursor()
    cursor.execute(sql_select)
    results = cursor.fetchall()
    for result in results:
        song_name = result[0]
        singer_name = result[1]
        vector = list(map(float, result[2].strip().split(",")))
        comments_num = int(result[4])
        songs[result[3]] = {"song_name": song_name, "singer_name": singer_name, "vector": vector, "comments_num": comments_num}
        song_ids.append(result[3])

    sql_delete = "DELETE FROM songs WHERE song_id = '%s' "
    # 去重操作
    del_num = 0
    i = 0
    while i < len(song_ids):
        j = i + 1
        song_i = songs.get(song_ids[i])
        while j < len(song_ids):
            song_j = songs.get(song_ids[j])
            sim = cal_cosine_sim(np.array(song_i.get("vector")), np.array(song_j.get("vector")))
            if sim >= 0.99:
                if song_i.get("comments_num") > song_j.get("comments_num"):
                    # 删除song_j
                    cursor.execute(sql_delete % song_ids[j])
                    song_ids.remove(song_ids[j])
                    del_num += 1
                else:
                    # 删除i
                    cursor.execute(sql_delete % song_ids[i])
                    song_ids.remove(song_ids[i])
                    i -= 1
                    del_num += 1
                    break
            else:
                j += 1
        i += 1
    connection.commit()
    print("去重完毕！del_num::", del_num)


if __name__ == '__main__':
    connection = pymysql.Connect(
        host="localhost",
        port=3306,
        user="root",
        passwd="root",
        db="personal_practice",
        charset="utf8"
    )
    deduplicate_data(connection)   # 歌曲去重
    song_singers = read_data(connection)
    save_to_redis(song_singers)    # 去重后结果存入Redis
    connection.close()


