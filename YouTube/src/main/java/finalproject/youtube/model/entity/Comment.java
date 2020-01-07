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
    private long   id;
    @Column(name = "text")
    private String text;
    @Column(name = "time_posted")
    private LocalDateTime timePosted;
    @Column(name = "video_id")
    private long videoId;
    @Column(name = "owner_id")
    private long ownerId;
    @Column(name = "replied_to_id")
    private long repliedToId;

}
