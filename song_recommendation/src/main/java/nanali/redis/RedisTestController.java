package nanali.redis;

/**
 * @date 2019-03-31 18:54
 * 内容来自https://blog.csdn.net/qq_22211217/article/details/80463053
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/redisTest")
public class RedisTestController {

    @Autowired
    RedisUtil redisUtil;

    /**
     *@Description: 测试redis
     *@return Object
     *@Author: zyj 2018/5/26 8:46
     */
    @RequestMapping("/testRedisGet")
    @ResponseBody
    Object testRedisGet(){
        return redisUtil.get("testlina");
    }

    @RequestMapping("/testRedisGetList")
    @ResponseBody
    List<Double> testRedisGetList(){
        List<Double> res = new ArrayList<>();
        if(redisUtil.get("lina") != null) {
            String result = redisUtil.get("lina").toString();
            String[] split_ress = result.split(",");
            for (String split_res : split_ress) {
                res.add(Double.valueOf(split_res));
            }
        }else{
            System.out.println("不存在");
        }
        return res;
    }
}