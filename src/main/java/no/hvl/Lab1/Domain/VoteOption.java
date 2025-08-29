package no.hvl.Lab1.Domain;

import java.util.Objects;
import java.util.UUID;

public class VoteOption {
    private UUID id;
    private UUID pollId;
    private String caption;
    private int presentationOrder;

    public VoteOption() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPollId() { return pollId; }
    public void setPollId(UUID pollId) { this.pollId = pollId; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public int getPresentationOrder() { return presentationOrder; }
    public void setPresentationOrder(int presentationOrder) { this.presentationOrder = presentationOrder; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VoteOption)) return false;
        VoteOption that = (VoteOption) o;
        return Objects.equals(id, that.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
