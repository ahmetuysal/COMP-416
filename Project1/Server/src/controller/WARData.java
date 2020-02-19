package controller;

import org.bson.Document;

public class WARData {

    private String userName;
    private int currentScore;
    private int[] remainingCards;

    public WARData(String userName, int[] remainingCards, int currentScore) {

        this.userName = userName;
        this.remainingCards = remainingCards;
        this.currentScore = currentScore;

    }

    public Document generateWARDoc() {
        Document doc2gen = new Document()
                .append("name", this.userName)
                .append("remaining", this.remainingCards)
                .append("score",currentScore);
        return doc2gen;
    }

    public WARData loadFromDoc(Document doc) {
        return new WARData(doc.getString("name"), (int[]) doc.get("remaining"), doc.getInteger("score"));
    }


}