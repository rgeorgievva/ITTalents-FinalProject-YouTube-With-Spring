package finalproject.youtube.model.entity;

public enum Category {

    MUSIC(1), SPORTS(2), PETS_AND_ANIMALS(3), TRAVEL_AND_EVENTS(4), GAMING(5),
    PEOPLE_AND_BLOGS(6), EDUCATION(7), SCIENCE(8), ENTERTAINMENT(9), COMEDY(10), OTHER(11);

    private static final long MIN_ID = 1;
    private static final long MAX_ID = 11;

    private long id;

    Category(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public static boolean isValidId(long id) {
        if (id < MIN_ID || id > MAX_ID) {
            return false;
        }

        return true;
    }
}
