package nanali.recommendation.controller;

import nanali.recommendation.repository.SongsRepo;
import nanali.recommendation.util.ConstantUtil;
import nanali.redis.RedisUtil;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import nanali.bean.Songs;
import nanali.recommendation.util.Utils;
import nanali.recommendation.service.RecommendationDao;

import javax.annotation.Resource;

/**
 * 歌曲推荐主逻辑。
 * @author lina
 * @date 2019-03-28 14:36
 */
@RestController
@RequestMapping("/recommend")
public class RecommendationCtrl {

    @Autowired
    SongsRepo songsRepo;

    @Autowired
    RedisUtil redisUtil;

    @Resource
    RecommendationDao recommendationDao;


    @RequestMapping(value="/get_recommend_res", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public ArrayList<JSONObject> getRecommend(@RequestParam("singer") String singer_name, @RequestParam("song") String song_name){
        /**
         * 根据用户的输入信息进行歌曲推荐。
         * 推荐逻辑：
         * 1、如果只输入歌手名：若歌手名在数据库中，则推荐此歌手最热的K首歌，都在alert提示信息
         * 2、如果输入歌手名和歌曲名/关键字，则：
         *      Step1：判断输入的是歌曲名还是关键字，如果是歌曲名则获取此歌曲的vector，如果没有此歌曲，则当做关键词，获取此关键词的vector
         *      Step2：基于step1的vector查找此歌手下面的topK相似歌曲。
         * 3、如果只输入歌曲名，则直接当做关键字，获取此歌手下面的topK歌曲
         * @author lina
         * @date 2019-03-28
         * @param singer_name  用户输入的singer name
         * @param song_name  用户输入的歌曲名/关键词
         * @return java.util.ArrayList<net.sf.json.JSONObject>
         */

        ArrayList<JSONObject> res = new ArrayList<>();

        song_name = song_name.toLowerCase();  //考虑出现英文，全部都换成小写
        singer_name = singer_name.toLowerCase();

        if (!singer_name.equals("") && (song_name.equals(""))){
            // 若只有singer_name, song_name为空，直接推荐此singer name的热歌
            List<Songs> songs_from_singer = songsRepo.getBySingerName(singer_name, ConstantUtil.REC_NUMBER);
            res = Utils.songToJson(songs_from_singer);
        }

        else if((!singer_name.equals("")) && (!song_name.equals(""))){
            //若singer_name和song_name都不为空
            /**
             * Step1：查询倒排索引，看唱过这首歌的所有歌手。
             * Step2: 若这种歌被当前歌手唱过，则mysql数据中获取歌曲的vector表示。否则，当做关键字获取关键字表示
             *        但是推荐的歌曲全部都是singer_name的。
             */

            if(recommendationDao.getSingersInvertedIndex(song_name) == null){
                //如果没有这首歌，则当做关键词，走按关键词查找的逻辑。
                res = recommendationDao.getRecommendRes(singer_name, song_name, ConstantUtil.REC_NUMBER, false);
            }else{
                //如果有这首歌，看这首歌是否在singers_list中，若在获取其vector向量，否则当做关键词
                List<String> singers_list = recommendationDao.getSingersInvertedIndex(song_name);
                if(singers_list.contains(singer_name)){
                    //若此singer_name在其中，则获取其vector向量。
                    res = recommendationDao.getRecommendRes(singer_name, song_name, ConstantUtil.REC_NUMBER, true);
                }
                else{
                    res = recommendationDao.getRecommendRes(singer_name, song_name, ConstantUtil.REC_NUMBER, false);
                }
            }
        }

        else if (singer_name.equals("") && (!song_name.equals(""))){
            // 若只有song_name, singer_name为空，输入字当做关键词搜索。
            res = recommendationDao.getRecommendRes("", song_name, ConstantUtil.REC_NUMBER, false);
            /*
            /**
             * Step1：查询倒排索引，看唱过这首歌的所有歌手。
             * Step2：若有1或者多个歌手唱过，则按照比例推荐歌曲，这部分逻辑和(!singer_name.equals("")) && (!song_name.equals(""))一样
             *        否则，若没有歌手，则认为是关键词，直接当成关键词查询。
             */ /*
            if(recommendationDao.getSingersInvertedIndex(song_name) == null){   //若没有作者，直接当做关键字
                res = recommendationDao.getRecommendRes("", song_name, ConstantUtil.REC_NUMBER, false);
            }else{
                List<String> singers_list = recommendationDao.getSingersInvertedIndex(song_name);
                int every = (int)(ConstantUtil.REC_NUMBER / singers_list.size());
                for(int i = 0; i < singers_list.size() - 1; i++){
                    ArrayList<JSONObject> res_every = recommendationDao.getRecommendRes(singers_list.get(i), song_name, every, true);
                    res.addAll(res_every);
                }
                ArrayList<JSONObject> res_final = recommendationDao.getRecommendRes(singers_list.get(singers_list.size() - 1), song_name, (ConstantUtil.REC_NUMBER - every * (singers_list.size() - 1)), true);
                res.addAll(res_final);
            }*/
        }
        return res;
    }

}
