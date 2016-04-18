package hcmut.cse.bookslover.models;

/**
 * Created by hoangdo on 4/18/16.
 */
public class User {
    private String _id;
    private String name;
    private String username;
    private String email;
    private boolean admin;
    private String avatar;

    public User() {}

    public String get_id() {
        return _id;
    }

    public boolean getAdmin() {
        return admin;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAbsoluteAvatarUrl() {
        return "http://api.ws.hoangdo.info/images/" + getAvatar();
    }

}
