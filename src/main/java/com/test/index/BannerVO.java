package com.test.index;

import lombok.*;

import java.sql.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BannerVO {
    public int ba_id;

    public Date created_at;
    public String ba_desc;
    public String ba_text;
    public String ba_tags;
    public String ba_img;
    public String ba_link;

    public List<String> tags;
}
