# songs-recommendation
基于语义信息和行为信息的歌曲推荐。包括歌曲信息爬取、数据处理、word2vec歌曲向量表示、Mysql歌曲信息存储、Redis倒排索引存储、歌曲推荐、web可视化展示。

说明：思想来自贺成同学，我进行了实现。

本练习具体说明请移步本人博客：https://blog.csdn.net/quiet_girl/article/details/89307282

## 一、文件说明

- data_crawling: 数据爬取文件，包括歌词爬取文件和歌曲评论数爬取文件（Python）。lyrics_\*是data_crawling.py的输出结果；comments_num为comments_num_crapy.py的输出信息。

- data_processing：歌词处理文件（Python）。lyrics_\*是执行data_processing.py之后的输出文件。

- word2vec：歌曲向量表示及数据存储 （Python）。word2vec_res.\*是emb_song.py执行之后的输出文件；cluster_result.txt是cluster.py执行之后的输出文件。

- song_recommendation：web可视化代码，前后端未分离（Java）。src/main/java/nanali中是java代码；src/main/resources/static中是js、css和html等文件。


## 二、使用说明

- data_crawling中文件执行顺序：data_crawling.py，comments_num_crapy.py

- word2vec中文件执行顺序：emb_song.py，get_song_vec_and_save_to_mysql.py，deduplicate_and_inverted_index.py,cluster.py（这个放在get_song_vec_and_save_to_mysql.py之后即可）

- song_recommendation执行方法：直接执行java文件Application.java即可，访问地址：localhost:8080

## 三、推荐逻辑

(1) 若只输入歌手名，则判断是否在数据库中，若在则推荐topK评论数最多的歌曲。否则，给出alert错误提示。

(2) 若同时给出了歌手名和歌曲名/关键字，则判断此歌曲是否被此歌手歌唱过，若是，则推荐此歌手与此首歌曲余弦相似度最高的K首歌。否则认为此输入信息是关键词，在Redis中获取此关键词的向量表示，推荐此歌手的topK余弦相似度歌曲。

(3) 若只输入歌曲名/关键词信息，则直接当做关键词进行推荐，在Redis中获取此关键词的向量表示，推荐此歌手的topK余弦相似度歌曲。

(4) 对于vocabulary不在数据库中的词语，给出alert报错信息。

## 四、web展示

1、主界面：支持歌手名和歌曲名/关键词输入

<div align=center><img src="https://github.com/Nana0606/songs-recommendation/blob/master/imgs/main.png" width="80%" alt="主界面"/></div>

2、只输入歌手信息，这里举例：周杰伦

<div align=center><img src="https://github.com/Nana0606/songs-recommendation/blob/master/imgs/only_singer.png" width="80%" alt="只输入歌手信息_周杰伦"/></div>

3、输入歌手信息和关键词

<div align=center><img src="https://github.com/Nana0606/songs-recommendation/blob/master/imgs/singer_and_song1.png" width="80%" alt="输入歌手和歌曲信息_邓紫棋/多远都要在一起"/></div>
<div align=center><img src="https://github.com/Nana0606/songs-recommendation/blob/master/imgs/singer_and_song2.png" width="80%" alt="输入歌手和歌曲信息_陈奕迅/爱情"/></div>

4、只输入关键词，这里举例：甜蜜、自由
<div align=center><img src="https://github.com/Nana0606/songs-recommendation/blob/master/imgs/only_keyword1.png" width="80%" alt="只输入关键词信息_甜蜜"/></div>
<div align=center><img src="https://github.com/Nana0606/songs-recommendation/blob/master/imgs/only_keyword2.png" width="80%" alt="只输入关键词信息_自由"/></div>

## 五、v2版本说明

暂时本系统主要基于训练而成的vector信息进行推荐，主要存在的问题如下：
>(1) 歌词信息不是很多，可能影响vector的训练质量。     
>(2) 歌词信息只涉及到歌曲的语义信息，因为此推荐基于纯语义，也就是说如果歌曲歌词比较类似，则会作为推荐结果。但是用户行为，比如用户喜好没有添加进来。

v2版本改进点：
>爬取网易云歌单信息，因为歌单是用户创建的，在很大程序上可以反映此用户的喜好，无论是歌词、歌曲风格、歌手等这都反映了用户的行为信息。后续将考虑使用歌单信息将每首歌表示成一个vector，这个vector则不仅考虑了语义信息，还包括用户喜好、歌曲风格等隐含信息。

未完待更~
