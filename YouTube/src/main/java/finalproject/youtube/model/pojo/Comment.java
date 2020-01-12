package finalproject.youtube.model.pojo;

import finalproject.youtube.model.dto.RequestCommentDto;
import finalproject.youtube.model.dto.ResponseCommentDto;
import finalproject.youtube.model.dto.ResponseReplyDto;
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
    @Column(name = "likes")
    private int likes;
    @Column(name = "dislikes")
    private int dislikes;

    public Comment(RequestCommentDto requestCommentDto){
        this.setText(requestCommentDto.getText());
        this.setTimePosted(LocalDateTime.now());
        this.setVideoId(requestCommentDto.getVideoId());
    }

    public ResponseCommentDto toDto() {
        return new ResponseCommentDto(this);
    }

    public ResponseReplyDto toReplyDto() {
        return new ResponseReplyDto(this);
    }
}
