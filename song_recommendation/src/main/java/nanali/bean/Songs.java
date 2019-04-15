package nanali.bean;

import javax.persistence.*;

/**
 * songs bean class.
 * @author lina
 * @date 2019-03-28 14:27
 */
@Table(name="songs")
@Entity
public class Songs {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    private int songId;
    private String songName;
    private String singerName;

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSingerName() {
        return singerName;
    }

    public void setSingerName(String singerName) {
        this.singerName = singerName;
    }

    @Override
    public String toString() {
        return "Songs{" +
                "songId=" + songId +
                ", songName='" + songName + '\'' +
                ", singerName='" + singerName + '\'' +
                '}';
    }
}
