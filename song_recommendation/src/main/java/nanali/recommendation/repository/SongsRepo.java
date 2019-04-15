package nanali.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import nanali.bean.Songs;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author lina
 * @date 2019-03-28 14:54
 */

public interface SongsRepo extends JpaRepository<Songs, Integer> {

    //获取singer name的评论数最多的K首歌
    @Query(value = "SELECT song_id, song_name, singer_name FROM songs WHERE singer_name= ?1 order by comments_num DESC limit ?2", nativeQuery = true)
    List<Songs> getBySingerName(String singer_name, int number);

    //获取singer_name+song_name下的vector向量
    @Query(value = "SELECT vector FROM songs WHERE singer_name = ?1 and song_name= ?2", nativeQuery = true)
    String getBySingerNameAndSongName(String singer_name, String song_name);

}
