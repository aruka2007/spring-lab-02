package org.example.springlab02.notify;

public interface Notifier {
    String send(String message);
    String channel();
}