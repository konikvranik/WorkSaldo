package net.suteren.worksaldo.model;

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by petr on 23.02.2016.
 */
public class UserDetail {
    public int id;
    public String api_token;
    public int default_wid;
    public String email;
    public String fullname;
    public String jquery_timeofday_format;
    public String jquery_date_format;
    public String timeofday_format;
    public String date_format;
    public Boolean store_start_and_stop_time;
    public int beginning_of_week;
    public Locale language;
    public String duration_format;
    public String image_url;
    public Date at;
    public Date created_at;
    public String timezone;
    public int retention;
    public Object new_blog_post;
    public List<Project> projects;
    public List<Tag> tags;
    public List<Task> tasks;
    public List<Workspace> workspaces;
    public List<Client> clients;
}
