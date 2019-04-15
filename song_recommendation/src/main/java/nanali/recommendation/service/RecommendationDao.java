package nanali.recommendation.service;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lina
 * @date 2019-04-01 08:55
 */

public interface RecommendationDao {
    List<JSONObject> getSongs(String sqlCommon, String[] columns);


    //flag = 1表示说明(singer_name,song_name)在mysql中，获取歌曲的信息。
    ArrayList<JSONObject> getRecommendRes(String singer_name, String song_name, int K, boolean flag);

    List<String> getSingersInvertedIndex(String song_name);
}
