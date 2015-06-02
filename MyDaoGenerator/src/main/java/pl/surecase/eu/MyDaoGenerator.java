package pl.surecase.eu;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

public class MyDaoGenerator {

    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(2, "fr.micklewright.smsvote.database");
        schema.enableKeepSectionsByDefault();

        /**
         * Election core entity
         */
        Entity election = schema.addEntity("Election");
        election.addIdProperty();
        election.addStringProperty("name").notNull();
        election.addStringProperty("description");
        election.addDateProperty("date").notNull();
        election.addIntProperty("stage");
        election.addIntProperty("registrationCode");

        /**
         * Post core entity
         */
        Entity post = schema.addEntity("Post");
        post.addIdProperty();
        post.addStringProperty("name");
        post.addIntProperty("places");

        /**
         * Application core entity
         */
        Entity application = schema.addEntity("Application");
        application.addIdProperty();
        application.addIntProperty("candidateNumber");

        /**
         * Contact core entity
         */
        Entity contact = schema.addEntity("Contact");
        contact.addLongProperty("number").primaryKey();
        contact.addStringProperty("name");

        /**
         * Participation join table = Contact ↔ Election
         */
        Entity participation = schema.addEntity("Participation");

        /**
         * Vote join table = Contact ↔ Application
         */
        Entity vote = schema.addEntity("Vote");


        /**
         * Entity relationships
         */
        // Election (N:1) Post
        Property electionId = post.addLongProperty("electionId").notNull().getProperty();
        post.addToOne(election, electionId);

        ToMany electionToPosts = election.addToMany(post, electionId);
        electionToPosts.setName("posts");

        // Post (N:1) Application
        Property postId = application.addLongProperty("postId").notNull().getProperty();
        application.addToOne(post, postId);

        ToMany postToApplication = post.addToMany(application, postId);
        postToApplication.setName("applications");

        // Contact (N:1) Application
        Property applicantId = application.addLongProperty("applicantId").notNull().getProperty();
        application.addToOne(contact, applicantId);

        ToMany applicantToApplication = contact.addToMany(application, applicantId);
        applicantToApplication.setName("applications");


        // Contact (N:N) Application = Vote
        Property voterId = vote.addLongProperty("contactNumber").notNull().getProperty();
        vote.addToOne(contact, voterId);

        ToMany contactToVotes = contact.addToMany(vote, voterId);
        contactToVotes.setName("votes");

        Property applicationId = vote.addLongProperty("applicationId").notNull().getProperty();
        vote.addToOne(application, applicationId);

        ToMany applicationToVotes = application.addToMany(vote, applicationId);
        applicationToVotes.setName("votes");

        // Contact (N:N) Election = Voter
        Property participationVoterId = participation.addLongProperty("contactNumber").notNull().getProperty();
        participation.addToOne(contact, participationVoterId);

        ToMany contactToParticipations = contact.addToMany(participation, participationVoterId);
        contactToParticipations.setName("participations");

        Property participationElectionId = participation.addLongProperty("electionId").notNull().getProperty();
        participation.addToOne(election, participationElectionId);

        ToMany electionToParticipations = election.addToMany(participation, participationElectionId);
        electionToParticipations.setName("participations");


        new DaoGenerator().generateAll(schema, args[0]);
    }
}
