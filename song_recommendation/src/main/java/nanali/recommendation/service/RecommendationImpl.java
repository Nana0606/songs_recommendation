package nanali.recommendation.service;

import nanali.recommendation.repository.SongsRepo;
import nanali.recommendation.util.ConstantUtil;
import nanali.recommendation.util.Utils;
import nanali.redis.RedisUtil;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lina
 * @date 2019-04-01 08:56
 */

@Service(value="recommendationService")
public class RecommendationImpl implements RecommendationDao{
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    SongsRepo songsRepo;

    @Override
    public List<JSONObject> getSongs(String sqlCommon, final String[] columns) {
        /**
         * 获取歌曲信息
         * @author lina
         * @date 2019-04-14
         * @param sqlCommon sql语句
         * @param columns  对应的mysql字段名以及需要存储到map中的key，这里设置为一样的。
         * @return java.util.List<net.sf.json.JSONObject>
         */
        List<JSONObject> res = jdbcTemplate.query(sqlCommon, new RowMapper<JSONObject>() {
            @Override
            public JSONObject mapRow(ResultSet resultSet, int i) throws SQLException {
                JSONObject rs = Utils.mapColsToJson(columns, resultSet);
                return rs;
            }
        });
        return res;
    }

    public List<Double> getAverageVec(String keywords){
        /**
         * 获取关键词的vector向量表示，表示方法：分词之后每个词的vector表示的均值。
         * @author lina
         * @date 2019-04-14
         * @param keywords 输入的关键词
         * @return java.util.List<java.lang.Double>  关键词的vector表示
         */
        // get the added input vector.
        List<String> cut_words = Utils.cutWords(keywords.toLowerCase());  //分词
        int count = 0;
        List<Double> res_vector = new ArrayList<>();
        for(int i = 0; i < cut_words.size(); i++){
            String cut_word = cut_words.get(i);
            if(redisUtil.hget(cut_word, "vector") != null) {  //Redis中有此词的表示
                count ++;
                String result = redisUtil.hget(cut_word, "vector").toString();
                String[] split_ress = result.split(",");
                if (split_ress.length == ConstantUtil.DIM){
                    if (i == 0){
                        for (String split_res : split_ress) {
                            res_vector.add(Double.valueOf(split_res));
                        }
                    }else{
                        for(int j = 0; j < split_ress.length; j++){
                            Double temp = res_vector.get(j);
                            res_vector.set(j, temp + Double.valueOf(split_ress[j]));
                        }
                    }

                }
            }
        }

        if (res_vector.size() > 0){   //若size>0，则说明关键词存在，否则返回vector为空，此种情况前端会提示alert
            // calculate the average value of input content as the final vector.
            for(int i = 0; i < ConstantUtil.DIM; i++){
                Double temp = res_vector.get(i);
                res_vector.set(i, Utils.div(temp, count, 16));
            }
        }

        return res_vector;
    }

    @Override
    public List<String> getSingersInvertedIndex(String song_name){
        /**
         * 获取singer的倒排索引，即获取这首歌被谁歌唱过。
         * @author lina
         * @date 2019-04-14
         * @param song_name 歌曲名称
         * @return java.util.List<java.lang.String>
         */
        List<String> singers = new ArrayList<>();
        String singers_str = "";
        if(redisUtil.hget(song_name, "singers") != null){
            singers_str = redisUtil.hget(song_name, "singers").toString();
            String[] singers_list = singers_str.split(",");
            for (String elem: singers_list){
                singers.add(elem);
            }
        }else{
            return null;
        }
        return null;
    }

    @Override
    public ArrayList<JSONObject> getRecommendRes(String singer_name, String song_name, int K, boolean flag) {
        /**
         * 根据限制条件获取推荐的topK的歌曲。
         * @author lina
         * @date 2019-04-14
         * @param singer_name 歌手名，推荐范围为此singer_name。若无，则设置为""，推荐范围为全部歌曲。
         * @param song_name  歌曲名，若无，则设置为""即可
         * @param K 推荐的歌曲数目
         * @param flag: flag=true表示存在此首歌曲；flag=false，表示这是一个关键词。
         * @return java.util.ArrayList<net.sf.json.JSONObject>
         */

        List<Double> res_vector = new ArrayList<>();
        //不对singer name进行限制，只使用song name作为关键字的信息。
        if(flag){
            //mysql中获取
            String vec_res = songsRepo.getBySingerNameAndSongName(singer_name, song_name);
            res_vector =Utils.strToList(vec_res);
        }else{
            res_vector = getAverageVec(song_name);
            if (res_vector.size() == 0){
                return new ArrayList<JSONObject>();   //此时关键词获取不到，返回空值
            }
        }

        // compare with all songs to get the topK recommendation results.
        List<JSONObject> songs = new ArrayList<>();
        if(singer_name.equals("")){   //全集
            String sqlCommon = "SELECT song_name, singer_name, vector, song_id, comments_num FROM songs WHERE song_name !='" + song_name + "' ";
            String[] columns = {"song_name", "singer_name", "vector", "song_id", "comments_num"};
            songs = getSongs(sqlCommon, columns);
        }else{   //基于此singer name的所有歌曲
            String sqlCommon = "SELECT song_name, singer_name, vector, song_id, comments_num FROM songs WHERE singer_name = '" + singer_name+ "' and song_name !='" + song_name + "' ";
            String[] columns = {"song_name", "singer_name", "vector", "song_id", "comments_num"};
            songs = getSongs(sqlCommon, columns);
        }

        Map<String, JSONObject> songs_similarity = new HashMap<>();  //存储所有candidate songs的信息

        for(JSONObject cur_song: songs){
            String cur_song_name = cur_song.get("song_name").toString();
            String cur_singer_name = cur_song.get("singer_name").toString();
            String cur_vector = cur_song.get("vector").toString();
            List<Double> cur_vector_lis = Utils.strToList(cur_vector);
            String cur_song_id = cur_song.get("song_id").toString();
            int cur_comments_num = Integer.valueOf(cur_song.get("comments_num").toString());
            Double similarity = Utils.calSimilarity(res_vector, cur_vector_lis);
            if(songs_similarity.get(cur_song_name) != null){   //若已经存在
                int has_comments_num = Integer.parseInt(songs_similarity.get(cur_song_name).get("comments_num").toString());
                if(has_comments_num < cur_comments_num){  //若歌曲名称有重复的，则保留最大的cur_comments_num对应的那个
                    JSONObject detail = Utils.mapElemToJson(cur_singer_name, cur_song_name, cur_song_id, cur_comments_num, similarity);
                    songs_similarity.put(cur_song_name, detail);
                }
            }else{
                JSONObject detail = Utils.mapElemToJson(cur_singer_name, cur_song_name, cur_song_id, cur_comments_num, similarity);
                songs_similarity.put(cur_song_name, detail);
            }
        }

        ArrayList<JSONObject> songs_similarity_topk = Utils.getTopK(songs_similarity, K);  //获取topK

        return songs_similarity_topk;
    }


}
