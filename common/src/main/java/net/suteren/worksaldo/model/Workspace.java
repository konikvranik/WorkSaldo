package net.suteren.worksaldo.model;

import java.util.Date;

/**
 * Created by petr on 23.02.2016.
 */
public class Workspace {
    public int id;
    public String name;
    public Date at;
    public int default_hourly_rate;
    public String default_currency;
    public boolean projects_billable_by_default;
    public int rounding;
    public int rounding_minutes;
    public String api_token;
}
