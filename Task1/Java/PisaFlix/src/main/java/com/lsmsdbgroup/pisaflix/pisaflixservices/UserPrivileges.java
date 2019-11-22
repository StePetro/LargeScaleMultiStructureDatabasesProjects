package com.lsmsdbgroup.pisaflix.pisaflixservices;

public enum UserPrivileges {
    NORMAL_USER(0), SOCIAL_MODERATOR(1), MODERATOR(2), ADMIN(3);

    private final int value;
    private UserPrivileges(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    
    @Override
    public String toString(){
        switch(this.value){
            case 1: return "Social Moderator";
            case 2: return "Moderator";
            case 3: return "Admin";
            default: return "User";
        }
    }
    
    public static String valueOf(int level){
        switch(level){
            case 1: return "Social Moderator";
            case 2: return "Moderator";
            case 3: return "Admin";
            default: return "User";
        }
    }
}
