package nanali.recommendation.util;

import com.huaban.analysis.jieba.JiebaSegmenter;
import nanali.bean.Songs;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.jcp.xml.dsig.internal.dom.DOMUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author lina
 * @date 2019-03-28 19:16
 */

public class Utils {

    public static List<String> cutWords(String content){
        /**
         * 分词
         * @author lina
         * @date 2019-04-14
         * @param content 需要进行分词的内容
         * @return java.util.List<java.lang.String>
         */
        JiebaSegmenter segmenter = new JiebaSegmenter();
        List<String> res = segmenter.sentenceProcess(content);
        return res;
    }

    public static double div(double d1, double d2, int len) {// 进行除法运算
        /**
         * 进行除法运算，保留len为小数，d1/d2
         * @author lina
         * @date 2019-04-14
         * @param d1 被除数
         * @param d2 除数
         * @param len 小数位数
         * @return double
         */
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.divide(b2,len,BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static JSONObject mapColsToJson(String[] columns, ResultSet rs) throws SQLException {
        /**
         * 将ResultSet转化为Json
         * @author lina
         * @date 2019-04-14
         * @param columns  字段名
         * @param rs
         * @return net.sf.json.JSONObject
         */
        JSONObject res = new JSONObject();
        for(int i = 0 ; i < columns.length; i++){
            res.put(columns[i], rs.getString(columns[i]));
        }
        return res;
    }


    public static List<Double> strToList(String str){
        /**
         * 因为vector在数据库中存储的字符拼接，这里将其转化为list
         * @author lina
         * @date 2019-04-14
          * @param str
         * @return java.util.List<java.lang.Double>
         */
        List<Double> res_vector = new ArrayList<>();
        String[] split_ress = str.split(",");
        if (split_ress.length == ConstantUtil.DIM){
            for(int i = 0; i < split_ress.length; i++){
                res_vector.add(Double.valueOf(split_ress[i]));
            }
        }
        return res_vector;
    }

    public static ArrayList<JSONObject> songToJson(List<Songs> songs_from_singer){
        /**
         * 将song类型封装成JSONobject
         * @author lina
         * @date 2019-04-14
         * @param songs_from_singer 歌曲信息
         * @return java.util.ArrayList<net.sf.json.JSONObject>
         */
        ArrayList<JSONObject> res = new ArrayList<>();
        for(int i = 0; i < songs_from_singer.size(); i++){
            Songs current = songs_from_singer.get(i);
            JSONObject temp = new JSONObject();
            temp.put("singer_name", current.getSingerName());
            temp.put("song_name", current.getSongName());
            temp.put("song_id", current.getSongId());
            res.add(temp);
        }
        return res;
    }

    public static Double calSimilarity(List<Double> vec1, List<Double> vec2){
        /**
         * 计算2个向量的余弦相似度
         * @author lina
         * @date 2019-04-14
         * @param vec1
         * @param vec2
         * @return java.lang.Double
         */
        double vec1Modulo = 0.00;//向量1的模
        double vec2Modulo = 0.00;//向量2的模
        double vectProduct = 0.00; //向量积
        for(int i = 0; i < ConstantUtil.DIM; i++){
            vec1Modulo += vec1.get(i) * vec1.get(i);
            vec2Modulo += vec2.get(i) * vec2.get(i);
            vectProduct += vec1.get(i) * vec2.get(i);
        }
        DecimalFormat df = new DecimalFormat(".00");
        double sim = Double.valueOf(df.format(vectProduct/(Math.sqrt(vec1Modulo)*Math.sqrt(vec2Modulo))));
        return sim;
    }


    public static ArrayList<JSONObject> getTopK(Map<String, JSONObject> songs_similarity, int k){
        /**
         * 从候选集中获取topK相似度的歌曲
         * @author lina
         * @date 2019-04-14
         * @param songs_similarity candidate songs map
         * @param k 选出的歌曲数
         * @return java.util.ArrayList<net.sf.json.JSONObject>
         */
        TreeMap<Double, ArrayList<JSONObject>> temp = new TreeMap<Double, ArrayList<JSONObject>>(new xbComparator());
        //TreeMap<Double, ArrayList<JSONObject>>， double表示相似度，JSONObject表示歌曲
        ArrayList<JSONObject> res_topK = new ArrayList<>();
        for(Map.Entry<String, JSONObject> entry : songs_similarity.entrySet()){
            JSONObject elem = entry.getValue();
            Double similarity = elem.getDouble("similarity");
            if(temp.containsKey(similarity)){
                ArrayList<JSONObject> objs = temp.get(similarity);
                objs.add(elem);
                temp.put(similarity, objs);
            }else{
                ArrayList<JSONObject> objs = new ArrayList<>();
                objs.add(elem);
                temp.put(similarity, objs);
            }
        }

        //选出topK相似度的歌曲
        int flag = 1;
        for(Map.Entry<Double, ArrayList<JSONObject>> entry: temp.entrySet()){
            ArrayList<JSONObject> objs = entry.getValue();
            for(JSONObject elem: objs){
                if (res_topK.size() < k){
                    res_topK.add(elem);
                }else{
                    flag = 0;
                    break;
                }
            }
            if (flag == 0){
                break;
            }
        }
        return res_topK;
    }


    public static JSONObject mapElemToJson(String singer_name, String song_name, String song_id, int comments_num, double similarity){
        /**
         * 给出element，组装成jsonobject
         * @author lina
         * @date 2019-04-14
         * @param singer_name
         * @param song_name
         * @param song_id
         * @param comments_num
         * @param similarity
         * @return net.sf.json.JSONObject
         */
        JSONObject detail = new JSONObject();
        detail.put("singer_name", singer_name);
        detail.put("song_name", song_name);
        detail.put("song_id", song_id);
        detail.put("comments_num", comments_num);
        detail.put("similarity", similarity);
        return detail;
    }

    public static void main(String[] args){
        Map<String, JSONObject> songs_similarity = new HashMap<>();
        JSONObject j1 = new JSONObject();
        j1.put("similarity", 5);
        j1.put("name", "lina5");
        songs_similarity.put("lina5", j1);
        JSONObject j2 = new JSONObject();
        j2.put("similarity", 20);
        j2.put("name", "lina20");
        songs_similarity.put("lina20", j2);
        JSONObject j3 = new JSONObject();
        j3.put("similarity", 4);
        j3.put("name", "lina4");
        songs_similarity.put("lina4", j3);
        JSONObject j4 = new JSONObject();
        j4.put("similarity", 16);
        j4.put("name", "lina16");
        songs_similarity.put("lina16", j4);
        JSONObject j5 = new JSONObject();
        j5.put("similarity", 9);
        j5.put("name", "lina9");
        songs_similarity.put("lina9", j5);
        int k = 2;
        List<JSONObject> res = getTopK(songs_similarity, k);
        for (JSONObject obj: res){
            System.out.println(obj.getString("name"));
        }
    }
}

class xbComparator implements Comparator {
    public int compare(Object o1,Object o2){
        Double i1=(Double)o1;
        Double i2=(Double)o2;
        return -i1.compareTo(i2);
    }
}