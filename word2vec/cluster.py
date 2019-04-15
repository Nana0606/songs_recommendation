# python3
# -*- coding: utf-8 -*-
# @Author  : lina
# @Time    : 2019-04-13 23:17
import pymysql
from sklearn.cluster import KMeans
import numpy as np

"""
为了测试word2vec的效果，这里将训练完毕的vector进行聚类，主要使用kmeans
"""


def read_data(connection):
    """
    从mysql数据库中songs表读取song_id, vector和song_name
    :param connection: mysql连接信息
    :return:
    """
    id_vectors = {}   # dict, key是id，value是vector向量
    id_song_names = {}    # dict，key是id，value是song name
    sql_select = "SELECT song_id, vector, song_name FROM songs"
    cursor = connection.cursor()
    cursor.execute(sql_select)
    results = cursor.fetchall()
    for result in results:
        id_vectors[result[0]] = list(map(float, result[1].strip().split(",")))
        id_song_names[result[0]] = result[2]
    return id_vectors, id_song_names


def cluster(song_vec, id_song):
    """
    聚类操作。
    :param song_vec: dict, key是id，value是vector向量
    :param id_song: key是id，value是song name
    :return:
    """
    song_id, arr = song_vec.keys(), np.array(list(song_vec.values()), dtype=np.float32)
    model = KMeans(n_clusters=500, verbose=1).fit(arr)
    labels = model.predict(arr)
    label_song = {}
    for tu in zip(song_id, labels):
        label_song.setdefault(tu[1], [])
        label_song[tu[1]].append(id_song[tu[0]])

    with open('./cluster_result.txt', 'a') as f:
        for k, v in label_song.items():
            f.write(str(k) + "\t" + str(v) + "\n")


if __name__ == '__main__':
    connection = pymysql.Connect(
        host='localhost',
        port=3306,
        user='root',
        passwd='root',
        db="personal_practice",
        charset='utf8'
    )
    id_vectors, id_song_names = read_data(connection)
    print(type(id_vectors.values()))
    connection.close()
    cluster(id_vectors, id_song_names)
