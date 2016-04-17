package hcmut.cse.bookslover.models;

import org.json.JSONObject;

/**
 * Created by hoangdo on 4/10/16.
 */
public class Book {
    private String _id;
    private String userId;
    private String title;
    private String author;
    private String publishYear;
    private String[] genres;
    private String review;
    private String cover;
    private String createdAt;
    private String updatedAt;

    public Book() {}

    public void set_id (String _id) {
        this._id = _id;
    }

    public String get_id() {
        return _id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getCover() {
        return cover;
    }

    public void setPublishYear(String publishYear) {
        this.publishYear = publishYear;
    }

    public String getPublishYear() {
        return publishYear;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    public String[] getGenres() {
        return genres;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getReview() {
        return review;
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

    public String getAbsoluteCoverUrl() {
        return "http://api.ws.hoangdo.info/images/" + getCover();
    }

}
