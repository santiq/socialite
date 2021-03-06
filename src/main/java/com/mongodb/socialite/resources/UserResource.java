package com.mongodb.socialite.resources;
import com.mongodb.socialite.api.Content;
import com.mongodb.socialite.api.ContentId;
import com.mongodb.socialite.api.FollowerCount;
import com.mongodb.socialite.api.FollowingCount;
import com.mongodb.socialite.api.User;
import com.mongodb.socialite.services.ContentService;
import com.mongodb.socialite.services.FeedService;
import com.mongodb.socialite.services.UserGraphService;
import com.mongodb.socialite.util.JSONParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    private final UserGraphService userGraph;
    private final FeedService feedService;
    private final ContentService contentService;

    public UserResource(ContentService content, FeedService feed, UserGraphService users ) {
        this.userGraph = users;
        this.feedService = feed;
        this.contentService = content;
    }

    @GET
    @Path("/{user_id}")
    public User get(@PathParam("user_id") String user_id ) {

        User user = this.userGraph.getUserById(user_id);
        return user;
    }

    @PUT
    @Path("/{user_id}")
    public User create(
            @PathParam("user_id") String userId, 
            @QueryParam("user_data") JSONParam userData ) {

        User newUser = new User(userId, userData);
        this.userGraph.createUser(newUser);
        return newUser;
    }

    @DELETE
    @Path("/{user_id}")
    public void delete(
            @PathParam("user_id") String userId, 
            @QueryParam("user_data") JSONParam userData ) {

        this.userGraph.removeUser(userId);
    }

    @GET
    @Path("/{user_id}/follower_count")
    public FollowerCount getFollowerCount(@PathParam("user_id") String user_id ) {
        this.userGraph.validateUser(user_id);
        return this.userGraph.getFollowerCount(new User(user_id));
    }

    @GET
    @Path("/{user_id}/followers")
    public List<User> getFollowers(@PathParam("user_id") String user_id,
            @DefaultValue("50") @QueryParam("limit") int limit) {
        this.userGraph.validateUser(user_id);
        return this.userGraph.getFollowers(new User(user_id), limit);
    }

    @GET
    @Path("/{user_id}/following")
    public List<User> getFriends(@PathParam("user_id") String user_id,
            @DefaultValue("50") @QueryParam("limit") int limit) {
        this.userGraph.validateUser(user_id);
        return this.userGraph.getFollowing(new User(user_id), limit);
    }

    @GET
    @Path("/{user_id}/following_count")
    public FollowingCount getFriendsCount(@PathParam("user_id") String user_id ) {
        this.userGraph.validateUser(user_id);
        return this.userGraph.getFollowingCount(new User(user_id));
    }

    @GET
    @Path("/{user_id}/friends_of_friends_agg")
    public List<User> getFriendsOfFriendsAgg(@PathParam("user_id") String user_id) {
        this.userGraph.validateUser(user_id);
        return this.userGraph.getFriendsOfFriendsAgg(new User(user_id));
    }

    @GET
    @Path("/{user_id}/friends_of_friends_query")
    public List<User> getFriendsOfFriendsQuery(@PathParam("user_id") String user_id) {
        this.userGraph.validateUser(user_id);
        return this.userGraph.getFriendsOfFriendsQuery(new User(user_id));
    }

    @PUT
    @Path("/{user_id}/following/{target}")
    public void follow(@PathParam("user_id") String user_id,
    		@PathParam("target") String to_follow ) {
        this.userGraph.validateUser(user_id);
        this.userGraph.validateUser(to_follow);
        this.userGraph.follow( new User(user_id), new User(to_follow) );
    }

    @DELETE
    @Path("/{user_id}/following/{target}")
    public void unfollow(@PathParam("user_id") String user_id,
            @PathParam("target") String to_unfollow ) {
        this.userGraph.validateUser(user_id);
        this.userGraph.validateUser(to_unfollow);
        this.userGraph.unfollow( new User(user_id), new User(to_unfollow) );
    }

    @POST
    @Path("/{user_id}/posts")
    public ContentId send(
            @PathParam("user_id") String user_id,
            @QueryParam("message") String message,
            @QueryParam("content") JSONParam data ) {

        this.userGraph.validateUser(user_id);
        User author = new User(user_id);
        Content newContent = new Content(author, message, data);
        
        // push to the content and feed services
        this.contentService.publishContent(author, newContent);
        this.feedService.post(author, newContent);
        return newContent.getContentId();
    }

    @GET
    @Path("/{user_id}/posts")
    public List<Content> getPosts(@PathParam("user_id") String user_id,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @QueryParam("anchor") ContentId anchor ) {
        this.userGraph.validateUser(user_id);
        return this.feedService.getPostsBy(new User(user_id), anchor, limit);
    }

    @GET
    @Path("/{user_id}/timeline")
    public List<Content> getTimeline(@PathParam("user_id") String user_id,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @QueryParam("anchor") ContentId anchor ) {
        this.userGraph.validateUser(user_id);
        return this.feedService.getFeedFor(new User(user_id), anchor, limit);
    }
}
