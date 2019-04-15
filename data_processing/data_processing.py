# python3
# -*- coding: utf-8 -*-
# @Author  : lina
# @Time    : 2018/12/2 11:32
"""
主要是对歌词进行处理，所有歌词和文件名保持不变：
1、空行
2、特殊字开头：
    ["编曲", "编辑", "歌名", "歌手", "专辑", "木吉他", "贝斯", "发行日", "曲", "监制", "制作人", "中乐演奏", "其余所有乐器演奏", "演奏", "和音",
                 "联合制作", "制作", "录音", "混音", "录音室", "混音室", "录音师", "混音师", "统筹", "制作统筹", "执行制作", "母带后期处理", "企划", "鼓",
                 "合声", "二胡", "乌克丽丽", "过带", "Bass", "Scratch", "OP", "Guitar", "SP", "Bass", "SCRATCH", "Programmer", "弦乐", "小提琴",
                 "女声", "Cello solo", "Piano", "吉他", "钢琴", "os", "弦乐", "和声", "DJ", "Tibet", "Violin", "Viola", "Cello", "和声", "母带",
                 "音乐", "打击乐", "Vocal", "次中音", "长号", "小号", "Music", "监制", "作词", "词/曲", "箫", "筝", "作词", "作曲", "Program", "键盘", "制作"]
3、将标点符号去除
"""
import os

count = 0

def contain_chinese(str):
    for s in str:
        if '\u4e00' <= s <= '\u9fa5':
            return True
    return False


def handle_lyrics(song_dir_path, song_dir, file, del_words):
    """
    对歌词进行处理。主要是去除一些异常字符、提示符等
    :param song_dir_path: 歌曲地址
    :param song_dir: 歌曲目录
    :param fPath: 歌词文件夹路径
    :param del_words: 需要去掉的内容
    :return:
    """
    if not os.path.exists(song_dir):
        os.mkdir(song_dir)
    global count
    with open(os.path.join(song_dir_path, file), 'r', encoding='utf8') as f:
        lines = f.readlines()
        content = ''
        for line in lines:
            flag_del = 0    # 此行是否需要去除
            for del_word in del_words:
                if del_word in line:
                    flag_del = 1    # 若有“作词”等开头的句子，说明不是歌词，则直接将此行去掉
                    break
            if flag_del == 1:
                    continue

            # 爬取到的特殊的换行符，注意：此符号不是“\n”
            space_except_char = """
            """
            space_except_char_2 = """
 """
            # 若一句话中含有特殊符号，则替换，若strip()之后为空，则说明是空格，不添加，否则添加（添加的时候不能strip()）
            if line.strip().replace(space_except_char, '').replace(space_except_char_2, '') != '':
                content += line.replace(space_except_char, '').replace(space_except_char_2, '')
        if contain_chinese(content) and content.strip() != '':   # 对于全英文的行，直接去除
            with open(os.path.join(song_dir, file), 'w', encoding='utf8') as w:
                w.write(content)
            w.close()
            print(song_dir, " ", file, '写入成功')
            count += 1
    f.close()

if __name__ == '__main__':
    del_words = ["编曲", "编辑", "歌名", "歌手", "专辑", "木吉他", "贝斯", "发行日", "曲", "监制", "制作人", "中乐演奏", "其余所有乐器演奏", "演奏", "和音",
                 "联合制作", "制作", "录音", "混音", "录音室", "混音室", "录音师", "混音师", "统筹", "制作统筹", "执行制作", "母带后期处理", "企划", "鼓",
                 "合声", "二胡", "乌克丽丽", "过带", "Bass", "Scratch", "OP", "Guitar", "SP", "Bass", "SCRATCH", "Programmer", "弦乐", "小提琴",
                 "女声", "Cello solo", "Piano", "吉他", "钢琴", "os", "弦乐", "和声", "DJ", "Tibet", "Violin", "Viola", "Cello", "和声", "母带",
                 "音乐", "打击乐", "Vocal", "次中音", "长号", "小号", "Music", "监制", "作词", "词/曲", "箫", "筝", "作词", "作曲", "Program", "键盘", "制作"]

    dir_path = "../data_crawling/"   # 歌词文件夹
    song_dirs = os.listdir(dir_path)
    for song_dir in song_dirs:   # 对所有文件夹/文件遍历
        if song_dir.startswith('lyrics_'):   # 判断是否是歌手文件夹
            song_dir_path = os.path.join(dir_path, song_dir)
            files = os.listdir(song_dir_path)
            for file in files:
                if file.startswith('歌曲名-'):
                    handle_lyrics(song_dir_path, song_dir, file, del_words)
    print("总歌曲数：", count)
