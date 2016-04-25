package hcmut.cse.bookslover.models;

/**
 * Created by huy on 4/25/2016.
 */
public class Meta {
    private int age = -1;
    private String website = "";

    public Meta() {
    }
    public String getWebsite(){
        return website;
    }
    public int getAge(){
        return age;
    }
    public void setWebsite(String website){
        this.website = website;
    }
    public void setAge(int age){
        this.age = age;
    }
}
