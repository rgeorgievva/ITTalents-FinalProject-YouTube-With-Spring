package finalproject.youtube.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table( name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int    id;
    @Column(name = "text")
    private String        text;
    @Column(name = "time_posted")
    private LocalDateTime timePosted;
    @Column(name = "video_id")
    private int videoId;
    @Column(name = "owner_id")
    private int ownerId;
    @Column(name = "replied_to_id")
    private int repliedToId;


    public Comment(String text, int ownerId) {
        this.text = text;
        this.ownerId = ownerId;
        this.timePosted = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Comment{ " +
                "text = '" + text + '\'' +
                ", timePosted = " + timePosted +
                ", videoId = " + videoId +
                ", ownerId = " + ownerId +
                ", repliedToId = " + repliedToId +
                '}';
    }
}
