package com.mycompany.model.responseModel;

import com.mycompany.model.app.Player;

import java.io.Serializable;

public class SendChallengeResponseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    boolean accepted;
    Player requestedPlayer;
    // The player who was challenged? Or the one responding?// Based on usage: responding to a challenge.
    // If I am B, I accept A.

    int challengerId; // ID of the player who sent the challenge (A)

    public SendChallengeResponseModel(boolean accepted, int challengerId) {
        this.accepted = accepted;
        this.challengerId = challengerId;
    }

    // Keeping existing field if necessary, but challengerId is crucial for routing
    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public int getChallengerId() {
        return challengerId;
    }

    public void setChallengerId(int challengerId) {
        this.challengerId = challengerId;
    }
}
