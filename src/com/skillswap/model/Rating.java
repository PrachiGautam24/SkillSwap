package com.skillswap.model;

public class Rating {
    private int id;
    private int bookingId;
    private int rating;
    private String review;
    private String createdAt;
    private boolean ratedByLearner; // true = learner rating tutor, false = tutor rating learner

    public Rating() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public boolean isRatedByLearner() { return ratedByLearner; }
    public void setRatedByLearner(boolean ratedByLearner) { this.ratedByLearner = ratedByLearner; }
}
