package com.softmed.ctc2extractor;

public class Constants {
    /**
     * Please UPDATE these authentication credentials with the correct values while running the app
     * This is a temporally implementation and will later be refactored to use opensrp authentication
     */
    public static final String USERNAME = "username";
    public static final String PASSWORD = "Password";

    public static final String BASE_SERVER_URL = "http://173.255.250.237:8082/opensrp/";



    /** These are testing server chw user and teams ids used to tie the clients to the specific chw and facility.
     *  These are temporarily hardcoded here for testing purposes for now
     *  but later will be refactored to a better implementation for obtaining them from opensrp server
     *  Please DO NOT UPDATE THESE for now while testing, or else the event and clients wont be tied to the chw, nor facility
     *  unless you have the correct values from the server.
     */
    public static final String locationID = "7504f24d-6b6f-4a7c-a8a2-60ab491678a6";
    public static final String providerId = "johnjamesdoe";
    public static final String teamId = "7d69862f-dde4-4ca0-bc24-27aff12e253a";
    public static final String team = "Masana Teams";

}
