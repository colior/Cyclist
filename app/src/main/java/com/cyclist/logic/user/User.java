package com.cyclist.logic.user;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String username;
    private String password;
    private String fName;
    private String lName;
    private int km;
    private Date birthday;
    private String home;
    private String work;
    private List<Favorite> favorites = new LinkedList<>();

    @Data
    public class Favorite
    {
        public String label;
        public String address;
    }
}
