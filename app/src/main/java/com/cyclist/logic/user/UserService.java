package com.cyclist.logic.user;

import com.cyclist.logic.firebase.DBService;

public class UserService {

    private static final String USERS_BUCKET = "Users";
    private DBService dbService = DBService.getInstance();

    public void save(User user){
        dbService.save(user, USERS_BUCKET);
    }

}
