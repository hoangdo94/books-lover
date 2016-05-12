package hcmut.cse.bookslover.models;

/**
 * Created by huy on 5/11/2016.
 */
public class Comment {

    private String _id;
    private User user;
    private String book;
    private String title;
    private String content;
    private String createdAt;
    private String updatedAt;

    public Comment() {}

    public void set_id (String _id) {
        this._id = _id;
    }

    public String get_id() {
        return _id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public User getUser(){
        return this.user;
    }

    public String getBook() {
        return this.book;
    }
}
