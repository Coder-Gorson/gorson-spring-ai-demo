package com.gorson.springai.security;

public interface CurrentUserProvider {

    CurrentUser requireCurrentUser();

}