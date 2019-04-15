# python3
# -*- coding: utf-8 -*-
# @Author  : lina
# @Time    : 2019-03-27 20:19

from gensim.models import word2vec
import os
import re
import jieba
import numpy as np
from nltk.corpus import stopwords
import json
import pymysql
from redis import Redis


"""
将训练的vector向量结果存储到Redis，将爬取到评论数以及获取的歌曲的vector存储到mysql
歌曲计算方式：取每首歌的所有词向量和之后去平均（停用词和频繁词去除）。
"""


EMB_DIM = 250   # embedding维度
res_global = {}   # key是去重过的(singer_name, song_name), value是(vector, song_id, comments_num)


def load_mode(file):
    """
    加载模型，并保存到vec_string_res中，为便于redis存储
    :param file:
    :return:model: word2vec模型
            vocabulary: word2vec包含的所有词汇
            vec_string_res：为便于Redis存储的数据格式，dict格式：{word, string of word2vec result}
    """
    model = word2vec.Word2Vec.load(file)
    vocabulary = model.wv.vocab.keys()   # model中包含的所有词语

    vec_res = {}
    vec_string_res = {}    # 存储到redis中，key is word，value is the vector string
    for elem in vocabulary:
        vector = model.wv.get_vector(elem)
        vec_res[elem] = vector.tolist()
        vec_string_res[elem.lower()] = "\"" + ','.join(map(str, vector.tolist())) + "\""

    vec_res_json_str = json.dumps(vec_res, indent=4)
    with open('./vector_res.json', 'w', encoding='utf8') as json_file:  # word2vec结果保存到文本文件中
        json_file.write(vec_res_json_str)
    # print(model.wv.get_vector('Edited'))

    return model, vocabulary, vec_string_res


def save_vocabulary_to_redis(vec_string_res):
    """
    将word2vec结果存储到Redis中，便于读取
    :param vec_string_res: dict格式：{word, string of word2vec result}
    :return:
    """
    redis = Redis(host='localhost', port=6379, db=0, decode_responses=True)
    # redis.mset(vec_string_res)   # 存储到Redis中
    for key, value in vec_string_res.items():
        redis.hset(key, "vector", value)

def save_song_to_mysql(connection):
    """
    将word2vec的结果保存到mysql中，保存字段包括singer_name, song_name, vector, song_id, comments_num
    :param singer_name: 歌手姓名
    :param song_name: 歌曲名称
    :param vector_str: 转化成string类型的word2vec结果
    :param connection: 数据库连接
    :param song_id: 歌曲id，这个是指在网易云音乐中的id
    :param comments_num: 歌曲的评论数目
    :return:
    """
    sql_insert = """
    INSERT INTO songs(song_name, singer_name, vector, song_id, comments_num) VALUES ('%s', '%s', '%s', '%s', %d)
    ON DUPLICATE KEY UPDATE comments_num=VALUES(comments_num);
    """
    # print("singer_name:%s, song_name:%s, vector_str::%s" % (singer_name, song_name.replace("'", "\\'"), vector_str))
    cursor = connection.cursor()
    for key, value in res_global.items():
        singer_name = key[0]
        song_name = key[1]  #  (vector_str, song_id.strip(), comments_num)
        vector_str = value[0]
        song_id = value[1]
        comments_num = value[2]
        # 将单引号编程转移字符
        cursor.execute(sql_insert % (song_name.replace("'", "\\'"), singer_name.replace("'", "\\'"), vector_str.replace("'", "\\'"), song_id, comments_num))
    connection.commit()
    print("插入完毕！")

def get_song_vector(content, model, singer_name, song_name, vocabulary, song_id, comments_num):
    """
    计算歌曲word2vec的结果。
    :param content: 歌曲内容
    :param model: word2vec模型结果
    :param singer_name: 歌手姓名
    :param song_name: 歌手名称
    :param vocabulary: word2vec包含的所有词汇
    :param connection: mysql数据库连接
    :param song_id: 歌曲id，此id是指在网易云音乐中的id
    :param comments_num: 歌曲评论数目
    :return:
    """
    punc = list("！？｡＂＃＄％＆＇（）＊＋，*《》－／：；＜＝＞＠［＼]-=＾＿｀︿｛｜｝~～｟｠｢｣､、〃》「」『』【】〔〕〖〗〘〙〚〛〜〝〞〟〰〾〿–—‘’‛“”„‟…‧﹏.→")
    punc2 = ["......", "..", "..."]
    punc = punc + punc2
    stop_words = ["我", "你", "他", "它", "的", "在", "是", "了", "都", " ", " ", "就", "也", "让",
                  "着", "这", "我们", "你们", "他们", "它们", "那", "和", "end", "music"
                                                                     'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p',
                  'a', 's', 'd',
                  'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm',
                  '1', '2', '3', '4', '5', '6', '7', '8', '9', '0']
    frequent_word = stopwords.words('english')
    song_cut = jieba.cut(content)   # 将歌曲分词
    # print(song_cut)
    count = 0
    added_vector = np.array([0.0] * EMB_DIM)
    for elem in song_cut:
        if elem.strip() != '' and elem.strip() not in frequent_word and elem.strip() not in punc + stop_words:
            if elem in vocabulary:
                vector = model.wv.get_vector(elem)
                added_vector += np.array(vector)
                count += 1
    final_vector = added_vector / count    # 歌曲中每个词的对应维度的平均值表示此歌曲的word2vec结果。
    vector_str = ','.join(map(str, final_vector.tolist()))
    song_name_obj = re.match(r'.*(-.*(live|Live|LIVE|Album Version|Version))', song_name.strip())    # 有些歌曲中含有 -live的字眼，将其去除
    if song_name_obj:
        temp = song_name_obj.group(1)
        song_name = song_name.replace(temp, '').strip()
    # save_song_to_mysql(singer_name.strip(), song_name.strip(), vector_str, connection, song_id.strip(), comments_num)
    if res_global.get((singer_name.strip(), song_name.strip())):
        if comments_num > res_global[(singer_name.strip(), song_name.strip())][2]:   # 若已经包含(singer,song)，即歌曲有重复，则保留最高评论数
            res_global[(singer_name.strip(), song_name.strip())] = (vector_str, song_id.strip(), comments_num)
    else:
        res_global[(singer_name.strip(), song_name.strip())] = (vector_str, song_id.strip(), comments_num)


def read_comments(comment_file_name):
    """
    读取评论数据
    :param comment_file_name:
    :return: comments_res：key is tuple of (singer_name, song_name)， value is (singer_id, comment_nums)
    """
    comments_res = {}   # key is (singer_name, song_name), value is (song_id, comments_num)
    with open(comment_file_name, 'r', encoding='utf8') as f:
        results = f.readlines()
        for result in results:
            temp_lis = [elem for elem in result.strip().split("\t") if elem != '']
            comments_res[(temp_lis[0], temp_lis[1])] = (temp_lis[2], int(temp_lis[3]))
    f.close()
    return comments_res


def main(model, vocabulary, connection):
    """
    读取数据，获取vector等，并存入mysql
    :param model: trained vector model
    :param vocabulary: word2vec中包含的所有key值
    :param connection: 数据库连接
    :return:
    """
    f_path = '../data_processing/'
    song_dirs = os.listdir(f_path)
    for song_dir in song_dirs:  # 歌手文件夹
        if song_dir.startswith('lyrics_'):
            # 获取歌手名称
            singer_name_obj = re.match(r'lyrics_(.*)', song_dir)
            if singer_name_obj.group():
                singer_name = singer_name_obj.group(1)
            else:
                singer_name = None

            comment_file_name = "../data_crawling/comments_num/singer_" + singer_name + ".txt"
            comments_info = read_comments(comment_file_name)
            song_file_path = os.path.join(f_path, song_dir)  # 歌手文件夹路径
            song_files = os.listdir(song_file_path)
            # 遍历每一首歌歌词
            for song_file in song_files:
                song_name_obj = re.match(r'歌曲名-(.*).txt', song_file)
                if song_name_obj.group():
                    song_name = song_name_obj.group(1)
                    if comments_info.get((singer_name, song_name)):   # 若在含有comment_num的列表中，则添加到数据库，否则直接忽略
                        with open(os.path.join(song_file_path, song_file), 'r', encoding='utf8') as f:
                            content = f.read()
                            get_song_vector(content, model, singer_name, song_name, vocabulary, comments_info[(singer_name, song_name)][0], comments_info[(singer_name, song_name)][1])
                        f.close()
            print(singer_name + "处理完毕")
    save_song_to_mysql(connection)

if __name__ == '__main__':
    model_path = './word2vec_res.model'    # word2vec模型结果存储地址
    model, vocabulary, vec_string_res = load_mode(model_path)
    save_vocabulary_to_redis(vec_string_res)
    connection = pymysql.Connect(
        host='localhost',
        port=3306,
        user='root',
        passwd='root',
        db="personal_practice",
        charset='utf8'
    )
    main(model, vocabulary, connection)
    connection.close()

