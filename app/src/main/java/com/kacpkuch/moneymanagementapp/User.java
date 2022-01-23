package com.kacpkuch.moneymanagementapp;

public class User implements Comparable<User>{
    private String id;
    private int balance;

    User(String id, int balance){
        this.id = id;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getBalance() {
        return balance;
    }
    public int getAbsBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", balance=" + balance +
                '}';
    }

    @Override
    public int compareTo(User o) {

        return (int)(o.getAbsBalance() - this.getAbsBalance());
    }

}
