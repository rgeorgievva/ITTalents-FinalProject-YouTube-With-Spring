package finalproject.youtube.model.entity;

import finalproject.youtube.model.dto.RequestCommentDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table( name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long   id;
    @Column(name = "text")
    private String text;
    @Column(name = "time_posted")
    private LocalDateTime timePosted;
    @Column(name = "video_id")
    private long videoId;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
    @ManyToOne
    @JoinColumn(name = "replied_to_id")
    private Comment repliedTo;

    public Comment(RequestCommentDto requestCommentDto){
        this.setText(requestCommentDto.getText());
        this.setTimePosted(LocalDateTime.now());
        this.setVideoId(requestCommentDto.getVideoId());
    }

}
