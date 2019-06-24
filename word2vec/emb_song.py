# python3
# -*- coding: utf-8 -*-
# @Author  : lina
# @Time    : 2019-03-27 19:47

import jieba
import jieba.analyse
import os
import logging
from gensim.models import word2vec

"""
生成embedding vector，将每一首歌中的每句话当做一个sentence，先进行分词，再基于分词结果进行训练。
"""


def read_data(f_path):
    """
    读取所有歌曲的歌词信息，并将其进入拼接
    :param f_path: 存储歌曲歌词的路径
    :return: all_content：所有歌曲的歌词信息
    """
    all_content = ''
    song_dirs = os.listdir(f_path)
    for song_dir in song_dirs:   # 歌手文件夹
        if song_dir.startswith('lyrics_'):
            song_file_path = os.path.join(f_path, song_dir)   # 歌手文件夹路径
            song_files = os.listdir(song_file_path)
            for song_file in song_files:   # 遍历每一首歌歌词
                with open(os.path.join(song_file_path, song_file), 'r', encoding='utf8') as f:
                    all_content += f.read()
                f.close()
    return all_content


def get_word_frequency(all_content):
    """
    获取分词之后的每个词出现的频率，为了方便后续常用词等的去除
    :param all_content: read_data(f_path)的结果
    :return:
    """
    word_frequency = {}
    all_content_cut = jieba.cut(all_content)
    for elem in all_content_cut:
        word_frequency.setdefault(elem, 1)
        word_frequency[elem] += 1
    word_frequency = sorted(word_frequency.items(), key=lambda x: x[1], reverse=True)
    with open('./word_frequency.txt', 'w', encoding='utf8') as w:
        for (key, value) in word_frequency:
            w.write(key + "\t" + str(value) + "\n")
    w.close()


def cut_words(all_content, segment_path):
    """
    分词。停用词和频繁词也在这个函数中去除。
    :param all_content:  read_data(f_path)的结果
    :param segment_path:  用于存储分词之后的结果的路径
    :return:
    """
    punc = list("！？｡＂＃＄％＆＇（）＊＋，*《》－／：；＜＝＞＠［＼]-=＾＿｀︿｛｜｝~～｟｠｢｣､、〃》「」『』【】〔〕〖〗〘〙〚〛〜〝〞〟〰〾〿–—‘’‛“”„‟…‧﹏.→")
    punc2 = ["......", "..", "..."]
    punc = punc + punc2
    stop_words = ["我", "你", "他", "它", "的", "在", "是", "了", "都", " ", " ", "就", "也", "让",
                  "着", "这", "我们", "你们", "他们", "它们", "那", "和", "end", "music",
                  'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd',
                  'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm',
                  '1', '2', '3', '4', '5', '6', '7', '8', '9', '0']
    new_cut_result = []
    all_content_cut = jieba.cut(all_content.lower())   # 分词
    for elem in all_content_cut:
        if elem.strip() not in punc and elem.strip() not in stop_words:
            new_cut_result.append(elem.strip())
    cut_result = ' '.join(new_cut_result)
    with open(segment_path, 'w', encoding='utf8') as w:
        w.write(cut_result)
    w.close()


def train(cut_result_file, model_path):
    """
    vector训练。
    :param cut_result_file: 分词之后的路径
    :param model_path: 用于存储model的路径地址
    :return:
    """
    logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)
    sentences = word2vec.LineSentence(cut_result_file)
    model = word2vec.Word2Vec(sentences, sg=1, hs=1, workers=1, min_count=1, window=5, size=250)
    model.save(model_path)


if __name__ == '__main__':
    f_path = '../data_processing/'
    all_content = read_data(f_path)
    segment_path = './all_content_segment.txt'
    cut_words(all_content, segment_path)
    model_path = './word2vec_res.model'
    train(segment_path, model_path)



