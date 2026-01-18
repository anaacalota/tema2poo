package entities.comments;

import lombok.Getter;

import java.time.LocalDate;

public class Comment {
    @Getter
    private final String author;
    @Getter
    private final String content;
    @Getter
    private final LocalDate createdAt;

    public Comment(final String author, final String content,
                   final LocalDate createdAt) {
        this.author = author;
        this.content = content;
        this.createdAt = createdAt;
    }
}
