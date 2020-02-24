package com.lsmsdbgroup.pisaflix.dbmanager;

import com.lsmsdbgroup.pisaflix.Entities.Entity;
import com.lsmsdbgroup.pisaflix.Entities.Film;
import com.lsmsdbgroup.pisaflix.Entities.Post;
import com.lsmsdbgroup.pisaflix.Entities.User;
import com.lsmsdbgroup.pisaflix.dbmanager.Interfaces.PostManagerDatabaseInterface;
import java.util.Date;
import java.util.Set;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import static org.neo4j.driver.v1.Values.parameters;

public class PostManager implements PostManagerDatabaseInterface {

    private static PostManager postManager;
    private final Driver driver;

    public static PostManager getIstance() {
        if (postManager == null) {
            postManager = new PostManager();
        }

        return postManager;
    }

    private PostManager() {
        driver = DBManager.getDB();
    }

    @Override
    public Post getPostFromRecord(Record record) {

        Post post = new Post();

        try {

            post.setIdPost(record.get("p").asNode().id());
            post.setText(record.get("p").get("Text").asString());

            try (Session session = driver.session()) {
                session.writeTransaction((Transaction t) -> setPostRelations(t, post));
            } catch (Exception ex) {
                System.out.println("Post relations retrieval error: " + ex.getLocalizedMessage());
            }

        } catch (Exception ex) {
            System.out.println("Post from record error: " + ex.getLocalizedMessage());
            System.out.println("Record not convertible: " + record);
        }

        return post;
    }

    private int setPostRelations(Transaction transaction, Post post) {

        StatementResult result = transaction.run("MATCH (u:User)-[c:CREATED]->(p:Post)-[:TAGS]->(:Film) "
                + "WHERE ID(p) = $id "
                + "RETURN u, c",
                parameters("id", post.getIdPost()));

        Record record = result.next();
        post.setUser(DBManager.userManager.getById(record.get("u").asNode().id()));
        post.setTimestamp(Date.from(record.get("c").get("Timestamp").asZonedDateTime().toInstant()));

        result = transaction.run("MATCH (:User)-[:CREATED]->(p:Post)-[:TAGS]->(f:Film) "
                + "WHERE ID(p) = $id "
                + "RETURN f",
                parameters("id", post.getIdPost()));

        while (result.hasNext()) {
            post.getFilmSet().add(DBManager.filmManager.getById(result.next().get("f").asNode().id()));
        }

        return 1;

    }

    @Override
    public Post getById(Long idPost) {

        Post post = null;

        try (Session session = driver.session()) {

            StatementResult result = session.run("MATCH (p:Post) "
                    + "WHERE ID(p) = $id "
                    + "RETURN p",
                    parameters("id", post.getIdPost()));

            post = getPostFromRecord(result.next());

        } catch (Exception ex) {
            System.out.println("Post retrieval error: " + ex.getLocalizedMessage());
        }

        return post;

    }

    @Override
    public void create(String text, User user, Set<Film> films) {
        try (Session session = driver.session()) {
            session.writeTransaction((Transaction t) -> createPostNode(t, text, user, films));
        } catch (Exception ex) {
            System.out.println("Post creation error: " + ex.getLocalizedMessage());
        }
    }

    private static int createPostNode(Transaction transaction, String text, User user, Set<Film> films) {
        StatementResult result = transaction.run("MATCH (u:User) "
                + "WHERE ID(u) = $userId "
                + "WITH u "
                + "CREATE (p:Post {Text: $text}) "
                + "CREATE (u)-[:CREATED {Timestamp: datetime()}]->(p) "
                + "RETURN ID(p) AS createdPostID",
                parameters("userId", user.getId(),
                        "text", text));

        if (!result.hasNext()) {
            return 0;
        }

        Long idPost = result.next().get("createdPostID").asLong();

        films.forEach((film) -> {
            transaction.run("MATCH (p: Post) "
                    + "WHERE ID(p) = $idPost "
                    + "WITH p "
                    + "MATCH (f: Film) "
                    + "WHERE ID(f) = $idFilm "
                    + "WITH p, f "
                    + "CREATE (p)-[:TAGS]->(f)",
                    parameters("idPost", idPost,
                            "idFilm", film.getId()));
        });

        return 1;
    }

    @Override
    public void delete(Long idPost) {
        try (Session session = driver.session()) {
            session.run("MATCH(p:Post) where ID(p) = $id "
                    + "MATCH (p)-[r]-() "
                    + "DELETE r, p",
                    parameters("id", idPost));
        } catch (Exception ex) {
            System.out.println("Delete post error: " + ex.getLocalizedMessage());
        }
    }

    @Override
    public void update(Long idPost, String text) {
        try (Session session = driver.session()) {
            session.run("MATCH (p:Post) where ID(p) = $id "
                    + "SET p.Text = $text "
                    + "WITH p "
                    + "MATCH ()-[r:CREATED]->(p) "
                    + "SET r.LastModified = datetime()",
                    parameters("id", idPost,
                            "text", text));
        } catch (Exception ex) {
            System.out.println("Update post error: " + ex.getLocalizedMessage());
        }
    }

    @Override
    public int count(Entity entity) {

        StatementResult result = null;

        if (entity.getClass() == Film.class) {
            try (Session session = driver.session()) {

                result = session.run("MATCH (p:Post)-[:TAGS]->(f:Film) "
                        + "WHERE ID(f) = " + entity.getId() + " "
                        + "RETURN count(DISTINCT p) AS count");

            } catch (Exception ex) {
                System.out.println("Related posts count error: " + ex.getLocalizedMessage());
            }
        }

        if (entity.getClass() == User.class) {
            try (Session session = driver.session()) {

                result = session.run("MATCH (u:User)-[:CREATED]->(p:Post) "
                        + "WHERE ID(f) = " + entity.getId() + " "
                        + "RETURN count(DISTINCT p) AS count");

            } catch (Exception ex) {
                System.out.println("Related posts count error: " + ex.getLocalizedMessage());
            }
        }

        return result.next().get("count").asInt();

    }

}
