package com.cyclist.logic.user;

import com.cyclist.logic.firebase.DBService;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserService {

    private static final String USERS_BUCKET = "Users";
    private static UserService instance;
    private DBService dbService = DBService.getInstance();

    public static UserService getInstance() {
        if (instance == null){
            instance = new UserService();
        }
        return instance;
    }

    public void save(User user){
        dbService.save(user, USERS_BUCKET);
    }
}
