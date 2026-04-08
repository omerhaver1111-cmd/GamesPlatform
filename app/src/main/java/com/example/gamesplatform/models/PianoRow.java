package com.example.gamesplatform.models;

public class PianoRow {
    public int correctIndex; // אינדקס (0-3)
    public boolean isBonus; // האם הקליד כחול

    public PianoRow(int correctIndex, boolean isBonus) {
        this.correctIndex = correctIndex;
        this.isBonus = isBonus;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }


}