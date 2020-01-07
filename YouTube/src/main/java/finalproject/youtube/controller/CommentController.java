package finalproject.youtube.controller;

import finalproject.youtube.model.dao.CommentDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class CommentController {

    @Autowired
    CommentDAO commentDAO;



}
