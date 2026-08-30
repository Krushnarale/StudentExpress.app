package com.core2web.repository;

import com.core2web.model.*;
import com.core2web.service.RentalService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataRepository {

    private static DataRepository instance;

    private User currentUser;
    private List<RoomItem> rooms = new ArrayList<>();
    private List<ProductItem> products = new ArrayList<>();
    private List<RoommateItem> roommates = new ArrayList<>();
    private List<ServiceItem> services = new ArrayList<>();
    private List<Booking> bookings = new ArrayList<>();
    private List<Order> orders = new ArrayList<>();
    private List<Rental> rentals = new ArrayList<>();
    private List<WalletTransaction> transactions = new ArrayList<>();
    private List<RoommateRequest> roommateRequests = new ArrayList<>();
    private List<SellerProfile> sellers = new ArrayList<>();
    private Set<String> savedRoomIds = new HashSet<>();
    private Set<String> savedProductIds = new HashSet<>();
    private double walletBalance = 1250.0;

    private DataRepository() {
        currentUser = null;
        seedData();
        new Thread(this::syncFromFirestore).start();
    }

    public static synchronized DataRepository getInstance() {
        if (instance == null) {
            instance = new DataRepository();
        }
        return instance;
    }

    private void seedData() {
        LocalDate today = LocalDate.now();

        // Seed Rooms & Rental Listings with Full Rental Attributes
        rooms.add(new RoomItem(
            "r1", "Single Room", "Kothrud, Pune", "₹ 6,000 / month", "2.4 km from you",
            "1 Occupant", "Rooms & PG", new String[]{"Furnished", "Wi-Fi", "Attached Bathroom", "24x7 Water"},
            "A spacious and fully furnished single room in a 2 BHK flat. Perfect for students and working professionals.",
            "Ramesh Sharma", "+91 98220 12345", "assets/image/room_single.png",
            "Monthly", 3, 12, 12000.0, today.minusDays(10), today.plusMonths(12), "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r2", "PG for Boys", "Hinjewadi, Pune", "₹ 7,500 / month", "4.1 km from you",
            "2 Occupants", "Rooms & PG", new String[]{"Furnished", "Wi-Fi", "Mess Available"},
            "Clean PG room with daily home cooked meals and high speed fiber internet.",
            "Suresh Patil", "+91 98500 67890", "assets/image/room_pg.png",
            "Monthly", 1, 6, 15000.0, today.minusDays(5), null, "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r3", "2 Sharing Room", "Baner, Pune", "₹ 4,500 / month", "3.6 km from you",
            "2 Occupants", "Rooms & PG", new String[]{"Furnished", "Wi-Fi", "Attached Bathroom"},
            "Bright student room with separate study desks, wardrobe and balcony view.",
            "Vikram Joshi", "+91 94221 44556", "assets/image/room_sharing.png",
            "Monthly", 2, 12, 9000.0, today, null, "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r4", "Studio Apartment", "Viman Nagar, Pune", "₹ 11,000 / month", "5.2 km from you",
            "1 Occupant", "Rooms & PG", new String[]{"Furnished", "Wi-Fi", "Kitchen", "AC"},
            "Modern studio apartment with wooden flooring, equipped kitchen and 24x7 security.",
            "Anil Deshmukh", "+91 91234 88990", "assets/image/room_studio.png",
            "Monthly", 6, 24, 22000.0, today.plusDays(5), null, "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r5", "1 BHK Flat", "Baner, Pune", "₹ 13,000 / month", "4.1 km from you",
            "2 Occupants", "Rooms & PG", new String[]{"Furnished", "Wi-Fi", "Balcony", "Parking"},
            "Cozy 1 BHK flat with living room sofa, coffee table, light wooden floor and natural light.",
            "Sneha Kulkarni", "+91 98765 43210", "assets/image/room_flat.png",
            "Monthly", 3, 12, 26000.0, today, null, "AVAILABLE"
        ));

        // Furniture Rentals
        rooms.add(new RoomItem(
            "r6", "Rent Study Table & Chair Set", "Kothrud, Pune", "₹ 299 / month", "1.8 km from you",
            "Student Rental", "Furniture", new String[]{"Free Delivery", "Flexible Tenure", "Maintenance Included"},
            "Ergonomic wooden study desk with comfortable mesh chair for monthly student rental.",
            "EasyRent Campus", "+91 91122 33445", "assets/image/table_study.png",
            "Monthly", 1, 12, 500.0, today.minusDays(30), null, "CURRENTLY_RENTED"
        ));
        rooms.add(new RoomItem(
            "r7", "Rent Ergonomic Office Mesh Chair", "Baner, Pune", "₹ 199 / month", "2.1 km from you",
            "Student Rental", "Furniture", new String[]{"Adjustable Height", "Lumbar Support", "Clean"},
            "High quality breathable mesh office chair for comfortable long study hours.",
            "Campus Furniture Rental", "+91 91133 44556", "assets/image/chair_office.png",
            "Monthly", 1, 6, 400.0, today, null, "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r8", "Rent Foldable Metal Bed & Mattress", "Hinjewadi, Pune", "₹ 399 / month", "3.0 km from you",
            "Student Rental", "Furniture", new String[]{"Cotton Mattress", "Heavy Duty Steel", "Portable"},
            "Sturdy single foldable bed frame with comfortable foam mattress included.",
            "Hostel Comforts", "+91 92244 55667", "assets/image/bed_foldable.png",
            "Monthly", 2, 12, 800.0, today, null, "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r9", "Rent 4-Tier Wooden Bookshelf", "Kothrud, Pune", "₹ 149 / month", "1.5 km from you",
            "Student Rental", "Furniture", new String[]{"Compact", "Spacious 4 Racks", "Sturdy"},
            "Wooden rack to organize textbooks, files, and study stationery in your room.",
            "BookRack Rentals", "+91 93355 66778", "assets/image/bookshelf.png",
            "Monthly", 1, 12, 300.0, today, null, "AVAILABLE"
        ));

        // Electronics Rentals
        rooms.add(new RoomItem(
            "r10", "Rent MacBook Air M1 Laptop", "Hinjewadi, Pune", "₹ 1,200 / day", "8.7 km from you",
            "Student Rental", "Electronics", new String[]{"8GB RAM", "256GB SSD", "M1 Chip"},
            "Apple MacBook Air M1 available for project work, coding assignments & editing.",
            "TechRent Pune", "+91 95566 77889", "assets/image/laptop_macbook.png",
            "Daily", 1, 30, 5000.0, today, null, "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r11", "Rent Dell i5 College Laptop", "Kothrud, Pune", "₹ 799 / month", "2.0 km from you",
            "Student Rental", "Electronics", new String[]{"Core i5", "8GB RAM", "Pre-loaded OS"},
            "Reliable Dell Intel Core i5 laptop for online classes, assignments and exams.",
            "LaptopOnRent Pune", "+91 96677 88990", "assets/image/laptop_dell.png",
            "Monthly", 1, 6, 2000.0, today, null, "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r12", "Rent Casio FX-991EX Scientific Calculator", "Kothrud, Pune", "₹ 99 / month", "1.2 km from you",
            "Student Rental", "Electronics", new String[]{"Engineering Standard", "Approved for Exams", "ClassWiz"},
            "Scientific calculator available for semester exam rentals and lab sessions.",
            "EduEquip Rentals", "+91 97788 99001", "assets/image/calculator.png",
            "Monthly", 1, 6, 200.0, today, null, "AVAILABLE"
        ));

        // Appliance Rentals
        rooms.add(new RoomItem(
            "r13", "Rent Mini Refrigerator 50L", "Hinjewadi, Pune", "₹ 450 / month", "3.2 km from you",
            "Student Rental", "Appliances", new String[]{"5-Star Energy", "Low Power", "Free Setup"},
            "Compact 50L mini fridge, perfect for hostel rooms & student apartments.",
            "ChillRent Appliances", "+91 92233 44556", "assets/image/fridge_mini.png",
            "Monthly", 1, 12, 1000.0, today, null, "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r14", "Rent Room Air Cooler 40L", "Baner, Pune", "₹ 599 / month", "2.9 km from you",
            "Student Rental", "Appliances", new String[]{"High Airflow", "Castor Wheels", "Low Noise"},
            "High performance desert air cooler with 40L water capacity for hot summer months.",
            "CoolBreeze Rental", "+91 93344 55667", "assets/image/air_cooler.png",
            "Monthly", 1, 6, 1200.0, today, null, "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r15", "Rent Microwave Oven 20L", "Viman Nagar, Pune", "₹ 350 / month", "4.5 km from you",
            "Student Rental", "Appliances", new String[]{"Auto Cook Menus", "Defrost", "Clean"},
            "Solo microwave oven for quick food heating, instant meal prep in student flats.",
            "HomeEase Rentals", "+91 94455 66778", "assets/image/microwave.png",
            "Monthly", 1, 6, 800.0, today, null, "AVAILABLE"
        ));

        // Books Rentals
        rooms.add(new RoomItem(
            "r16", "Rent Engineering Mathematics M1-M2 Textbook", "Kothrud, Pune", "₹ 60 / month", "1.4 km from you",
            "Student Rental", "Books", new String[]{"Semester 1 & 2", "Clear Notes", "Reference Guide"},
            "Standard M1 & M2 engineering math textbook available for monthly exam rent.",
            "LibraryOnRent", "+91 95566 77889", "assets/image/book_math.png",
            "Monthly", 1, 6, 100.0, today, null, "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r17", "Rent GATE CS Complete Preparation Kit", "Viman Nagar, Pune", "₹ 120 / month", "3.8 km from you",
            "Student Rental", "Books", new String[]{"All Subjects", "Solved Papers", "Formula Book"},
            "Comprehensive GATE Computer Science study material bundle for semester rental.",
            "GatePrep Hub", "+91 96677 88990", "assets/image/book_gate.png",
            "Monthly", 1, 6, 200.0, today, null, "AVAILABLE"
        ));

        // Vehicle Rentals
        rooms.add(new RoomItem(
            "r18", "Rent Yamaha R15 V4 Bike", "Kharadi, Pune", "₹ 2,500 / day", "6.2 km from you",
            "Student Rental", "Vehicles", new String[]{"Helmet Included", "Insured", "Valid License Req."},
            "Sleek black Yamaha R15 V4 sport bike available for daily and weekend rentals.",
            "RiderHub Pune", "+91 94455 66778", "assets/image/bike_yamaha.png",
            "Daily", 1, 7, 3000.0, today, null, "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r19", "Rent Hero Sprint Gear Bicycle", "Wakad, Pune", "₹ 199 / month", "2.8 km from you",
            "Student Rental", "Vehicles", new String[]{"21 Speed", "Dual Disc Brakes", "Free Servicing"},
            "21-Speed gear cycle for daily campus commute and quick errands.",
            "CampusCycles", "+91 97788 99001", "assets/image/cycle_hero.png",
            "Monthly", 1, 12, 500.0, today, null, "AVAILABLE"
        ));

        // Gym & Fitness Rentals
        rooms.add(new RoomItem(
            "r20", "Adjustable Dumbbells (20kg)", "Kothrud, Pune", "₹ 299 / month", "1.6 km from you",
            "Student Rental", "Gym & Fitness", new String[]{"Rubber Coated", "Selector Locks", "Home Gym"},
            "Pair of 20kg adjustable dumbbells with selector plates for home workout.",
            "FitRent Pune", "+91 98899 00112", "assets/image/dumbbells.png",
            "Monthly", 1, 12, 500.0, today, null, "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r21", "Foldable Treadmill", "Baner, Pune", "₹ 899 / month", "3.1 km from you",
            "Student Rental", "Gym & Fitness", new String[]{"Digital Display", "Foldable", "Quiet Motor"},
            "Compact motorized foldable treadmill ideal for room cardio workouts.",
            "FitRent Pune", "+91 98899 00112", "assets/image/treadmill.png",
            "Monthly", 1, 12, 1500.0, today, null, "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r22", "Exercise Bike", "Hinjewadi, Pune", "₹ 499 / month", "2.8 km from you",
            "Student Rental", "Gym & Fitness", new String[]{"LCD Monitor", "Adjustable Seat", "Magnetic Resistance"},
            "Indoor stationary fitness workout cycle with adjustable seat and resistance knob.",
            "FitRent Pune", "+91 98899 00112", "assets/image/exercise_bike.png",
            "Monthly", 1, 12, 800.0, today, null, "AVAILABLE"
        ));

        // Additional Appliance & Furniture Rentals
        rooms.add(new RoomItem(
            "r23", "Water Heating Rod", "Kothrud, Pune", "₹ 79 / month", "1.2 km from you",
            "Student Rental", "Appliances", new String[]{"Immersion Heater", "Fast Heating", "Shock Proof"},
            "Indosafe waterproof immersion water heating rod for instant hot water in hostels.",
            "StudentEssentials", "+91 98899 00112", "assets/image/water_heater_rod.png",
            "Monthly", 1, 6, 150.0, today, null, "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r24", "RO Water Purifier", "Baner, Pune", "₹ 299 / month", "2.1 km from you",
            "Student Rental", "Appliances", new String[]{"RO + UV + UF", "Mineral Booster", "Free Maintenance"},
            "Multi-stage wall-mounted water purifier for clean and safe drinking water.",
            "PureWater Rent", "+91 97788 99001", "assets/image/water_purifier.png",
            "Monthly", 1, 12, 600.0, today, null, "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r25", "Velvet Armchair", "Hinjewadi, Pune", "₹ 249 / month", "2.5 km from you",
            "Student Rental", "Furniture", new String[]{"Soft Velvet", "Golden Legs", "Comfortable"},
            "Stylish pink velvet upholstered armchair with gold metal legs for cozy reading.",
            "LuxeCampus Furniture", "+91 96677 88990", "assets/image/modern_armchair.png",
            "Monthly", 1, 12, 500.0, today, null, "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r26", "Ceiling Fan", "Viman Nagar, Pune", "₹ 149 / month", "3.0 km from you",
            "Student Rental", "Appliances", new String[]{"5 Blades", "LED Light", "Remote Control"},
            "Modern dark wooden 5-blade ceiling fan with central LED light ring.",
            "CoolBreeze Rental", "+91 95566 77889", "assets/image/ceiling_fan.png",
            "Monthly", 1, 12, 300.0, today, null, "AVAILABLE"
        ));
        rooms.add(new RoomItem(
            "r27", "Study Desk Set", "Kothrud, Pune", "₹ 349 / month", "1.7 km from you",
            "Student Rental", "Furniture", new String[]{"Built-in Bookshelf", "Drawers Included", "Matching Chair"},
            "Wooden study desk set with multi-tier bookshelf, double drawers, and matching chair.",
            "EasyRent Campus", "+91 94455 66778", "assets/image/study_desk_set.png",
            "Monthly", 1, 12, 700.0, today, null, "AVAILABLE"
        ));

        // Seed Products
        products.add(new ProductItem(
            "p1", "Engineering Math Book", "₹ 250", "Kothrud, Pune", "2 hours ago",
            "Books", "Good Condition", "Semester 1 & 2 M1-M2 standard textbook with clear notes.", "Kunal Deshmukh", "+91 98221 11223", "assets/image/book_math.png"
        ));
        products.add(new ProductItem(
            "p2", "Data Structures Book", "₹ 400", "Kothrud, Pune", "4 hours ago",
            "Books", "Like New", "Standard DSA textbook covering C++, Java, and algorithms with clean notes.", "Aman Verma", "+91 98111 22334", "assets/image/book_dsa.png"
        ));
        products.add(new ProductItem(
            "p3", "GATE CS Guide", "₹ 550", "Viman Nagar, Pune", "6 hours ago",
            "Books", "Good Condition", "Complete GATE CS preparation guide with solved previous year questions.", "Pooja Sharma", "+91 97222 33445", "assets/image/book_gate.png"
        ));
        products.add(new ProductItem(
            "p4", "Dell i5 Laptop", "₹ 18,000", "Hinjewadi, Pune", "5 hours ago",
            "Electronics", "Used", "Fast i5 processor laptop, 256GB SSD, great battery backup.", "Priya Singh", "+91 91234 56789", "assets/image/laptop_dell.png"
        ));
        products.add(new ProductItem(
            "p5", "MacBook Air M1", "₹ 52,000", "Hinjewadi, Pune", "1 day ago",
            "Electronics", "Like New", "Prinstine silver MacBook Air M1 with battery health 94%, box & charger included.", "Ketan Joshi", "+91 96333 44556", "assets/image/laptop_macbook.png"
        ));
        products.add(new ProductItem(
            "p6", "iPhone 11", "₹ 22,000", "Hinjewadi, Pune", "3 days ago",
            "Electronics", "Used", "Original iPhone 11 with Box, battery health 87%, pristine screen.", "Deepak Verma", "+91 97665 44332", "assets/image/iphone_11.png"
        ));
        products.add(new ProductItem(
            "p7", "Boat Earbuds", "₹ 899", "Pimpri, Pune", "2 days ago",
            "Electronics", "Good Condition", "True wireless earbuds with long playback time and clear audio.", "Rahul More", "+91 90111 22334", "assets/image/airpods_boat.png"
        ));
        products.add(new ProductItem(
            "p8", "Scientific Calculator", "₹ 750", "Kothrud, Pune", "1 day ago",
            "Electronics", "Like New", "ClassWiz natural textbook display scientific calculator essential for engineering exams.", "Siddharth N.", "+91 95444 55667", "assets/image/calculator.png"
        ));
        products.add(new ProductItem(
            "p9", "Study Table", "₹ 1,200", "Baner, Pune", "1 day ago",
            "Furniture", "Like New", "Sturdy wooden table with drawers, ideal for studying.", "Nilesh Patil", "+91 98223 33445", "assets/image/table_study.png"
        ));
        products.add(new ProductItem(
            "p10", "Office Chair", "₹ 1,800", "Baner, Pune", "3 days ago",
            "Furniture", "Good Condition", "Ergonomic mesh chair with adjustable height and lumbar support.", "Akash Shinde", "+91 94220 99887", "assets/image/chair_office.png"
        ));
        products.add(new ProductItem(
            "p11", "Wooden Bookshelf", "₹ 950", "Kothrud, Pune", "2 days ago",
            "Furniture", "Good Condition", "Compact 4-shelf wooden rack for college textbooks, files and decor.", "Neha Patel", "+91 94555 66778", "assets/image/bookshelf.png"
        ));
        products.add(new ProductItem(
            "p12", "Foldable Metal Bed", "₹ 1,600", "Hinjewadi, Pune", "2 days ago",
            "Furniture", "Good Condition", "Heavy duty steel foldable bed frame with wooden slats, easy to move.", "Rohan Deshmukh", "+91 93666 77889", "assets/image/bed_foldable.png"
        ));
        products.add(new ProductItem(
            "p13", "Adjustable Dumbbells (20kg)", "₹ 1,800", "Kothrud, Pune", "4 hours ago",
            "Gym & Fitness", "Like New", "Solid cast iron adjustable dumbbells pair with rubber grip handle.", "Varun K.", "+91 98777 66554", "assets/image/dumbbells.png"
        ));
        products.add(new ProductItem(
            "p14", "Resistance Bands", "₹ 499", "Baner, Pune", "6 hours ago",
            "Gym & Fitness", "Brand New", "Set of 5 color-coded workout resistance bands with doorway pull-up bar.", "Fitness Store Pune", "+91 97666 55443", "assets/image/resistance_bands.png"
        ));
        products.add(new ProductItem(
            "p15", "Exercise Bike", "₹ 6,500", "Hinjewadi, Pune", "1 day ago",
            "Gym & Fitness", "Like New", "Compact indoor exercise bike with heart rate sensors and pulse monitor.", "Tanmay S.", "+91 96555 44332", "assets/image/exercise_bike.png"
        ));
        products.add(new ProductItem(
            "p19", "Water Heating Rod", "₹ 349", "Kothrud, Pune", "2 hours ago",
            "Appliances", "Brand New", "Indosafe 1500W waterproof immersion water heating rod with safety plug.", "Omkar Jagtap", "+91 98225 55667", "assets/image/water_heater_rod.png"
        ));
        products.add(new ProductItem(
            "p20", "RO Water Purifier", "₹ 3,999", "Baner, Pune", "4 hours ago",
            "Appliances", "Like New", "Kent RO+UV mineral water purifier with 8L storage tank in prime working condition.", "Sameer K.", "+91 98221 33445", "assets/image/water_purifier.png"
        ));
        products.add(new ProductItem(
            "p21", "Velvet Armchair", "₹ 1,899", "Hinjewadi, Pune", "1 day ago",
            "Furniture", "Like New", "Plush pink velvet accent chair with brass gold metal legs.", "Pooja M.", "+91 97112 44556", "assets/image/modern_armchair.png"
        ));
        products.add(new ProductItem(
            "p22", "Ceiling Fan", "₹ 1,250", "Viman Nagar, Pune", "1 day ago",
            "Appliances", "Good Condition", "5-Blade dark wooden decorative ceiling fan with warm LED light.", "Rahul V.", "+91 96223 55667", "assets/image/ceiling_fan.png"
        ));
        products.add(new ProductItem(
            "p23", "Study Desk Set", "₹ 2,999", "Kothrud, Pune", "2 days ago",
            "Furniture", "Like New", "Complete wooden study station with upper bookshelf racks, storage drawers, and study chair.", "Aditya P.", "+91 95334 66778", "assets/image/study_desk_set.png"
        ));
        products.add(new ProductItem(
            "p17", "Hero Sprint Cycle", "₹ 3,500", "Wakad, Pune", "1 day ago",
            "Cycles", "Like New", "Gear cycle in top condition with dual disc brakes.", "Sanket Kale", "+91 93456 78901", "assets/image/cycle_hero.png"
        ));
        products.add(new ProductItem(
            "p18", "Skybags Backpack", "₹ 650", "Kothrud, Pune", "2 days ago",
            "Fashion", "Good Condition", "Lightweight campus laptop backpack with water resistance.", "Sneha Rao", "+91 98234 56711", "assets/image/backpack_skybags.png"
        ));

        // Roommates - strictly loaded from Firestore / user registrations
        // (No hardcoded sample roommate profiles)

        // Seed Services across 5 Campus Categories (Multiple providers per category)
        // 1. Laundry Providers
        services.add(new ServiceItem(
            "s1_1", "🧺", "Express Hostel Laundry & Steam Iron", "Laundry", "Doorstep pickup & 24h delivery",
            "₹ 15 / kg", "Express Wash Hub", "+91 98811 22334", "Complete wash & fold, sanitized steam ironing with doorstep delivery within 24 hours."
        ));
        services.add(new ServiceItem(
            "s1_2", "🧺", "Sparkle Cleaners & Dry Wash", "Laundry", "Premium dry clean & delicate wash",
            "₹ 25 / kg", "Sparkle Clean Laundry", "+91 98822 33445", "Special student discount on bulk laundry, blazers, lab coats, and bedsheets."
        ));
        services.add(new ServiceItem(
            "s1_3", "🧺", "StudentSaver Wash Station", "Laundry", "Monthly student subscription pack",
            "₹ 499 / month", "StudentSaver Laundry", "+91 98833 44556", "Includes 4 washes a month up to 20kg with free fabric conditioner and express fold."
        ));

        // 2. Tiffin / Mess Providers
        services.add(new ServiceItem(
            "s2_1", "🍱", "Annapurna Home Tiffin Service", "Tiffin / Mess", "Homestyle pure veg & hygienic meals",
            "₹ 2,500 / month", "Annapurna Home Tiffin", "+91 94230 11223", "Daily hot tiffin with 4 rotis, 2 vegetables, dal, rice, salad, and Sunday special sweet."
        ));
        services.add(new ServiceItem(
            "s2_2", "🍱", "Royal Feast Student Mess", "Tiffin / Mess", "Unlimited Veg & Non-Veg Thali",
            "₹ 3,200 / month", "Royal Student Mess", "+91 94231 22334", "Buffet dining with unlimited servings, North Indian & Maharashtrian dishes near campus."
        ));
        services.add(new ServiceItem(
            "s2_3", "🍱", "HealthyBites Diet & Fitness Tiffin", "Tiffin / Mess", "High protein low-oil student meals",
            "₹ 2,800 / month", "HealthyBites Kitchen", "+91 94232 33445", "Tailored student meal plan with sprouts, paneer, brown rice, curd and fresh veggies."
        ));

        // 3. Cleaning Providers
        services.add(new ServiceItem(
            "s3_1", "🧹", "CleanNest PG & Room Deep Cleaning", "Cleaning", "Deep Room Cleaning & Sanitization",
            "₹ 350 / session", "CleanNest Services", "+91 95522 33445", "Floor scrubbing, bathroom acid wash, cobweb removal, and balcony cleaning."
        ));
        services.add(new ServiceItem(
            "s3_2", "🧹", "QuickMaid Campus Care", "Cleaning", "Weekly Hostel Housekeeping",
            "₹ 599 / month", "QuickMaid Care", "+91 95523 44556", "Weekly scheduled visits for dusting, mopping, bedsheet changes and trash disposal."
        ));
        services.add(new ServiceItem(
            "s3_3", "🧹", "SanitizePro Bathroom & Room Polish", "Cleaning", "Bathroom Deep Scrub & Odor Fix",
            "₹ 299 / session", "SanitizePro Pune", "+91 95524 55667", "Specialized washroom deep scrub, tile whitening, tap descaling and odor elimination."
        ));

        // 4. Wi-Fi Providers
        services.add(new ServiceItem(
            "s4_1", "📶", "SpeedNet Fiber Broadband", "Wi-Fi", "100 Mbps Unlimited Fiber Internet",
            "₹ 499 / month", "SpeedNet Broadband", "+91 96633 44556", "100 Mbps fiber connection with zero installation charges and free dual-band Wi-Fi router."
        ));
        services.add(new ServiceItem(
            "s4_2", "📶", "AirFiber Ultra-Fast Campus Wi-Fi", "Wi-Fi", "200 Mbps Dedicated Student Line",
            "₹ 699 / month", "AirFiber Network", "+91 96634 55667", "Low ping dedicated line for coding, video lectures, online tests & streaming."
        ));
        services.add(new ServiceItem(
            "s4_3", "📶", "HostelLink Wireless Hotspot", "Wi-Fi", "Plug & Play Portable Wi-Fi Dongle",
            "₹ 399 / month", "HostelLink Tech", "+91 96635 66778", "Portable wireless 4G/5G router with unlimited daily data for hostel rooms."
        ));

        // 5. Repair & Maintenance Providers
        services.add(new ServiceItem(
            "s5_1", "🛠️", "QuickFix Home & Appliance Repair", "Repair & Maintenance", "Electrician, Fan & Cooler Repair",
            "₹ 149 / visit", "QuickFix Services", "+91 97744 55667", "Instant doorstep electrician service for room fans, coolers, tube lights and sockets."
        ));
        services.add(new ServiceItem(
            "s5_2", "🛠️", "Campus Plumb & Tap Repairs", "Repair & Maintenance", "Plumbing & Geyser Maintenance",
            "₹ 199 / visit", "Campus Plumber Hub", "+91 97745 66778", "Fast fixing for leaking taps, flush tanks, washbasin pipes and water heaters."
        ));
        services.add(new ServiceItem(
            "s5_3", "🛠️", "GadgetDoc Laptop & Phone Care", "Repair & Maintenance", "Laptop Hardware & OS Fixes",
            "₹ 249 + parts", "GadgetDoc Tech Care", "+91 97746 77889", "On-site laptop screen, keyboard replacement, SSD upgrade, thermal paste & OS formatting."
        ));

        // Seed Saved Items
        savedRoomIds.add("r1");
        savedRoomIds.add("r3");
        savedProductIds.add("p1");
        savedProductIds.add("p4");
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }

    public List<RoomItem> getRooms() { return rooms; }
    public void addRoom(RoomItem room) { rooms.add(0, room); }
    public void removeRoom(String id) { rooms.removeIf(r -> r.getId().equals(id)); }

    public List<ProductItem> getProducts() { return products; }
    public void addProduct(ProductItem product) { products.add(0, product); }
    public void removeProduct(String id) { products.removeIf(p -> p.getId().equals(id)); }
    public List<ProductItem> getProductsBySeller(String sellerUid) {
        List<ProductItem> list = new ArrayList<>();
        if (sellerUid == null || sellerUid.trim().isEmpty()) return list;
        for (ProductItem p : products) {
            if (sellerUid.equalsIgnoreCase(p.getSellerUid())) {
                list.add(p);
            }
        }
        return list;
    }
    public void addOrUpdateProduct(ProductItem product) {
        if (product == null) return;
        int idx = -1;
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(product.getId())) {
                idx = i;
                break;
            }
        }
        if (idx >= 0) {
            products.set(idx, product);
        } else {
            products.add(0, product);
        }
    }

    public List<SellerProfile> getSellers() { return sellers; }
    public SellerProfile getSellerProfile(String sellerId) {
        if (sellerId == null || sellerId.trim().isEmpty()) return null;
        for (SellerProfile s : sellers) {
            if (sellerId.equalsIgnoreCase(s.getSellerId())) {
                return s;
            }
        }
        return null;
    }
    public void addOrUpdateSeller(SellerProfile seller) {
        if (seller == null) return;
        int idx = -1;
        for (int i = 0; i < sellers.size(); i++) {
            if (sellers.get(i).getSellerId().equalsIgnoreCase(seller.getSellerId())) {
                idx = i;
                break;
            }
        }
        if (idx >= 0) {
            sellers.set(idx, seller);
        } else {
            sellers.add(0, seller);
        }
    }
    public void removeSeller(String sellerId) {
        sellers.removeIf(s -> s.getSellerId().equalsIgnoreCase(sellerId));
    }

    public List<RoommateItem> getRoommates() { return roommates; }
    public void addRoommate(RoommateItem roommate) { roommates.add(0, roommate); }
    public RoommateItem getRoommateForUser(String userUid) {
        if (userUid == null || userUid.trim().isEmpty()) return null;
        for (RoommateItem rm : roommates) {
            if (userUid.equalsIgnoreCase(rm.getUserUid())) {
                return rm;
            }
        }
        return null;
    }
    public void addOrUpdateRoommate(RoommateItem rm) {
        if (rm == null) return;
        int idx = -1;
        for (int i = 0; i < roommates.size(); i++) {
            if (roommates.get(i).getId().equals(rm.getId()) || (rm.getUserUid() != null && rm.getUserUid().equalsIgnoreCase(roommates.get(i).getUserUid()))) {
                idx = i;
                break;
            }
        }
        if (idx >= 0) {
            roommates.set(idx, rm);
        } else {
            roommates.add(0, rm);
        }
    }
    public void removeRoommate(String id) {
        roommates.removeIf(rm -> rm.getId().equals(id) || (id != null && id.equalsIgnoreCase(rm.getUserUid())));
    }

    public List<RoommateRequest> getRoommateRequests() { return roommateRequests; }
    public List<RoommateRequest> getIncomingRoommateRequests(String receiverUid) {
        List<RoommateRequest> list = new ArrayList<>();
        if (receiverUid == null || receiverUid.trim().isEmpty()) return list;
        for (RoommateRequest r : roommateRequests) {
            if (receiverUid.equalsIgnoreCase(r.getReceiverStudentId())) {
                list.add(r);
            }
        }
        return list;
    }
    public void addRoommateRequest(RoommateRequest request) {
        if (request == null) return;
        roommateRequests.add(0, request);
    }
    public void updateRoommateRequestStatus(String requestId, String status) {
        for (RoommateRequest r : roommateRequests) {
            if (r.getRequestId().equals(requestId)) {
                r.setStatus(status);
                break;
            }
        }
    }

    public List<ServiceItem> getServices() { return services; }
    public void addService(ServiceItem service) { services.add(0, service); }
    public void removeService(String id) { services.removeIf(s -> s.getId().equals(id)); }

    public List<Booking> getBookings() { return bookings; }
    public void addBooking(Booking booking) { bookings.add(0, booking); }
    public void removeBooking(String id) { bookings.removeIf(b -> b.getId().equals(id)); }

    public List<Order> getOrders() { return orders; }
    public void addOrder(Order order) { orders.add(0, order); }
    public List<Order> getOrdersForSeller(String sellerUid) {
        List<Order> list = new ArrayList<>();
        if (sellerUid == null || sellerUid.trim().isEmpty()) return list;
        for (Order o : orders) {
            if (sellerUid.equalsIgnoreCase(o.getSellerUid())) {
                list.add(o);
            }
        }
        return list;
    }
    public List<Order> getOrdersForBuyer(String buyerUid) {
        List<Order> list = new ArrayList<>();
        if (buyerUid == null || buyerUid.trim().isEmpty()) return list;
        for (Order o : orders) {
            if (buyerUid.equalsIgnoreCase(o.getBuyerUid())) {
                list.add(o);
            }
        }
        return list;
    }
    public void addOrUpdateOrder(Order order) {
        if (order == null) return;
        int idx = -1;
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getId().equals(order.getId())) {
                idx = i;
                break;
            }
        }
        if (idx >= 0) {
            orders.set(idx, order);
        } else {
            orders.add(0, order);
        }
    }
    public void updateOrderStatus(String orderId, String status) {
        for (Order o : orders) {
            if (o.getId().equals(orderId)) {
                o.setStatus(status);
                break;
            }
        }
    }

    public synchronized void syncFromFirestore() {
        try {
            // 1. Rooms / Rentals from Firestore
            List<RoomItem> fsRooms = new com.core2web.dao.RoomDAOImpl().findAll();
            if (fsRooms != null && !fsRooms.isEmpty()) {
                for (RoomItem r : fsRooms) {
                    if (rooms.stream().noneMatch(existing -> existing.getId().equals(r.getId()))) {
                        rooms.add(0, r);
                    }
                }
            }

            // 2. Products from Firestore
            List<ProductItem> fsProds = new com.core2web.dao.ProductDAOImpl().findAll();
            if (fsProds != null && !fsProds.isEmpty()) {
                for (ProductItem p : fsProds) {
                    int idx = -1;
                    for (int i = 0; i < products.size(); i++) {
                        if (products.get(i).getId().equals(p.getId())) {
                            idx = i;
                            break;
                        }
                    }
                    if (idx >= 0) {
                        products.set(idx, p);
                    } else {
                        products.add(0, p);
                    }
                }
            }

            // 3. Services from Firestore
            List<ServiceItem> fsServices = new com.core2web.dao.ServiceDAOImpl().findAll();
            if (fsServices != null && !fsServices.isEmpty()) {
                for (ServiceItem s : fsServices) {
                    if (services.stream().noneMatch(existing -> existing.getId().equals(s.getId()))) {
                        services.add(0, s);
                    }
                }
            }

            // 4. Roommates from Firestore
            List<RoommateItem> fsRoommates = new com.core2web.dao.RoommateDAOImpl().findAll();
            if (fsRoommates != null && !fsRoommates.isEmpty()) {
                for (RoommateItem rm : fsRoommates) {
                    int idx = -1;
                    for (int i = 0; i < roommates.size(); i++) {
                        if (roommates.get(i).getId().equals(rm.getId())) {
                            idx = i;
                            break;
                        }
                    }
                    if (idx >= 0) {
                        roommates.set(idx, rm);
                    } else {
                        roommates.add(0, rm);
                    }
                }
            }

            // 5. Rentals from Firestore
            List<Rental> fsRentals = new com.core2web.dao.RentalDAOImpl().findAll();
            if (fsRentals != null && !fsRentals.isEmpty()) {
                for (Rental rent : fsRentals) {
                    int idx = -1;
                    for (int i = 0; i < rentals.size(); i++) {
                        if (rentals.get(i).getRentalId().equals(rent.getRentalId())) {
                            idx = i;
                            break;
                        }
                    }
                    if (idx >= 0) {
                        rentals.set(idx, rent);
                    } else {
                        rentals.add(0, rent);
                    }
                }
            }

            // 6. Bookings from Firestore
            List<Booking> fsBookings = new com.core2web.dao.BookingDAOImpl().findAll();
            if (fsBookings != null && !fsBookings.isEmpty()) {
                for (Booking b : fsBookings) {
                    int idx = -1;
                    for (int i = 0; i < bookings.size(); i++) {
                        if (bookings.get(i).getId().equals(b.getId())) {
                            idx = i;
                            break;
                        }
                    }
                    if (idx >= 0) {
                        bookings.set(idx, b);
                    } else {
                        bookings.add(0, b);
                    }
                }
            }

            // 7. Roommate Requests from Firestore
            List<RoommateRequest> fsReqs = new com.core2web.dao.RoommateRequestDAOImpl().findAll();
            if (fsReqs != null && !fsReqs.isEmpty()) {
                for (RoommateRequest req : fsReqs) {
                    int idx = -1;
                    for (int i = 0; i < roommateRequests.size(); i++) {
                        if (roommateRequests.get(i).getRequestId().equals(req.getRequestId())) {
                            idx = i;
                            break;
                        }
                    }
                    if (idx >= 0) {
                        roommateRequests.set(idx, req);
                    } else {
                        roommateRequests.add(0, req);
                    }
                }
            }

            // 8. Orders / Requests from Firestore
            List<Order> fsOrders = new com.core2web.dao.OrderDAOImpl().findAll();
            if (fsOrders != null && !fsOrders.isEmpty()) {
                for (Order o : fsOrders) {
                    int idx = -1;
                    for (int i = 0; i < orders.size(); i++) {
                        if (orders.get(i).getId().equals(o.getId())) {
                            idx = i;
                            break;
                        }
                    }
                    if (idx >= 0) {
                        orders.set(idx, o);
                    } else {
                        orders.add(0, o);
                    }
                }
            }

            // 9. Sellers from Firestore
            List<SellerProfile> fsSellers = new com.core2web.dao.SellerDAOImpl().findAll();
            if (fsSellers != null && !fsSellers.isEmpty()) {
                for (SellerProfile s : fsSellers) {
                    int idx = -1;
                    for (int i = 0; i < sellers.size(); i++) {
                        if (sellers.get(i).getSellerId().equalsIgnoreCase(s.getSellerId())) {
                            idx = i;
                            break;
                        }
                    }
                    if (idx >= 0) {
                        sellers.set(idx, s);
                    } else {
                        sellers.add(0, s);
                    }
                }
            }
        } catch (Throwable e) {
            System.err.println("[DataRepository] Error syncing from Firestore: " + e.getMessage());
        }
    }

    public List<Rental> getRentals() {
        RentalService.autoUpdateRentalStatuses(rentals, rooms);
        return rentals;
    }

    public void addRental(Rental rental) {
        rentals.add(0, rental);
        new Thread(() -> new com.core2web.dao.RentalDAOImpl().save(rental)).start();
    }

    public List<Rental> getRentalsForStudent(String studentKey) {
        List<Rental> list = new ArrayList<>();
        if (studentKey == null || studentKey.trim().isEmpty()) return list;
        String clean = studentKey.trim();
        for (Rental r : getRentals()) {
            if (clean.equalsIgnoreCase(r.getStudentId()) || clean.equalsIgnoreCase(r.getStudentEmail())) {
                list.add(r);
            }
        }
        return list;
    }

    public List<Rental> getRentalsForOwner(String ownerKey) {
        List<Rental> list = new ArrayList<>();
        if (ownerKey == null || ownerKey.trim().isEmpty()) return list;
        String clean = ownerKey.trim();
        for (Rental r : getRentals()) {
            if (clean.equalsIgnoreCase(r.getOwnerId()) || clean.equalsIgnoreCase(r.getOwnerName())) {
                list.add(r);
            }
        }
        return list;
    }

    public Rental findRentalById(String rentalId) {
        for (Rental r : rentals) {
            if (r.getRentalId().equals(rentalId)) return r;
        }
        return null;
    }

    public void updateRentalStatus(String rentalId, String newStatus) {
        Rental r = findRentalById(rentalId);
        if (r != null) {
            r.setRentalStatus(newStatus);
            if ("ACCEPTED".equalsIgnoreCase(newStatus) || "ACTIVE".equalsIgnoreCase(newStatus)) {
                r.setPaymentStatus("PAID");
                // Mark Room as currently rented
                for (RoomItem item : rooms) {
                    if (item.getId().equals(r.getItemId())) {
                        item.setAvailabilityStatus("CURRENTLY_RENTED");
                        break;
                    }
                }
            } else if ("REJECTED".equalsIgnoreCase(newStatus) || "CANCELLED".equalsIgnoreCase(newStatus)) {
                // Keep available
                for (RoomItem item : rooms) {
                    if (item.getId().equals(r.getItemId())) {
                        if (!"CURRENTLY_RENTED".equals(item.getAvailabilityStatus())) {
                            item.setAvailabilityStatus("AVAILABLE");
                        }
                        break;
                    }
                }
            }
            new Thread(() -> new com.core2web.dao.RentalDAOImpl().updateStatus(rentalId, newStatus)).start();
        }
    }

    public void updateBookingStatus(String bookingId, String newStatus) {
        for (Booking b : bookings) {
            if (b.getId().equals(bookingId)) {
                b.setStatus(newStatus);
                break;
            }
        }
        new Thread(() -> new com.core2web.dao.BookingDAOImpl().updateStatus(bookingId, newStatus)).start();
    }

    public boolean requestRentalExtension(String rentalId, int additionalDuration) {
        Rental r = findRentalById(rentalId);
        if (r != null) {
            r.setExtensionDuration(additionalDuration);
            r.setExtensionStatus("PENDING");
            LocalDate newEnd = RentalService.calculateEndDate(r.getEndDate(), additionalDuration, r.getRentType());
            r.setNewEndDate(newEnd);
            r.setRentalStatus("EXTENSION_REQUESTED");
            return true;
        }
        return false;
    }

    public boolean approveRentalExtension(String rentalId) {
        Rental r = findRentalById(rentalId);
        if (r != null && r.getExtensionDuration() != null && r.getNewEndDate() != null) {
            r.setEndDate(r.getNewEndDate());
            r.setDuration(r.getDuration() + r.getExtensionDuration());
            r.setTotalAmount(r.getTotalAmount() + (r.getRentAmount() * r.getExtensionDuration()));
            r.setExtensionStatus("APPROVED");
            r.setExtensionDuration(null);
            r.setNewEndDate(null);

            // Recheck status
            long daysLeft = RentalService.calculateDaysRemaining(r.getEndDate());
            if (daysLeft <= 30) {
                r.setRentalStatus("EXPIRING_SOON");
            } else {
                r.setRentalStatus("ACTIVE");
            }
            return true;
        }
        return false;
    }

    public boolean rejectRentalExtension(String rentalId) {
        Rental r = findRentalById(rentalId);
        if (r != null) {
            r.setExtensionStatus("REJECTED");
            r.setExtensionDuration(null);
            r.setNewEndDate(null);
            
            // Revert back to ACTIVE or EXPIRING_SOON based on date
            long daysLeft = RentalService.calculateDaysRemaining(r.getEndDate());
            if (daysLeft <= 30) {
                r.setRentalStatus("EXPIRING_SOON");
            } else {
                r.setRentalStatus("ACTIVE");
            }
            return true;
        }
        return false;
    }

    public List<WalletTransaction> getTransactions() { return transactions; }
    public void addTransaction(WalletTransaction tx) { transactions.add(0, tx); }

    public double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(double balance) { this.walletBalance = balance; }
    public void addFunds(double amount) { this.walletBalance += amount; }

    public Set<String> getSavedRoomIds() { return savedRoomIds; }
    public boolean toggleSavedRoom(String id) {
        if (savedRoomIds.contains(id)) {
            savedRoomIds.remove(id);
            return false;
        } else {
            savedRoomIds.add(id);
            return true;
        }
    }

    public Set<String> getSavedProductIds() { return savedProductIds; }
    public boolean toggleSavedProduct(String id) {
        if (savedProductIds.contains(id)) {
            savedProductIds.remove(id);
            return false;
        } else {
            savedProductIds.add(id);
            return true;
        }
    }
}
