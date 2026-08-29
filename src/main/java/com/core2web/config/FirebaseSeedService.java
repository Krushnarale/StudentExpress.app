package com.core2web.config;

import com.core2web.dao.*;
import com.core2web.model.*;
import com.google.cloud.firestore.Firestore;
import java.util.List;

public class FirebaseSeedService {

    public static void seedIfEmpty() {
        new Thread(() -> {
            try {
                Firestore db = FirebaseConfig.getFirestore();
                if (db == null) return;



                // 2. Rooms & Rentals
                RoomDAO roomDAO = new RoomDAOImpl();
                List<RoomItem> rooms = roomDAO.findAll();
                if (rooms.isEmpty()) {
                    System.out.println("[FirebaseSeedService] Seeding initial rooms...");
                    roomDAO.save(new RoomItem("r1", "Single Room", "Kothrud, Pune", "₹ 6,000 / month", "2.4 km from you", "1 Occupant", "Rooms & PG", new String[]{"Furnished", "Wi-Fi", "Attached Bathroom", "24x7 Water"}, "A spacious and fully furnished single room in a 2 BHK flat. Perfect for students and working professionals.", "Ramesh Sharma", "+91 98220 12345", "assets/image/room_single.png", "2"));
                    roomDAO.save(new RoomItem("r2", "PG for Boys", "Hinjewadi, Pune", "₹ 7,500 / month", "4.1 km from you", "2 Occupants", "Rooms & PG", new String[]{"Furnished", "Wi-Fi", "Mess Available"}, "Clean PG room with daily home cooked meals and high speed fiber internet.", "Suresh Patil", "+91 98500 67890", "assets/image/room_pg.png", "2"));
                    roomDAO.save(new RoomItem("r3", "2 Sharing Room", "Baner, Pune", "₹ 4,500 / month", "3.6 km from you", "2 Occupants", "Rooms & PG", new String[]{"Furnished", "Wi-Fi", "Attached Bathroom"}, "Bright student room with separate study desks, wardrobe and balcony view.", "Vikram Joshi", "+91 94221 44556", "assets/image/room_sharing.png", "2"));
                    roomDAO.save(new RoomItem("r4", "Studio Apartment", "Viman Nagar, Pune", "₹ 11,000 / month", "5.2 km from you", "1 Occupant", "Rooms & PG", new String[]{"Furnished", "Wi-Fi", "Kitchen", "AC"}, "Modern studio apartment with wooden flooring, equipped kitchen and 24x7 security.", "Anil Deshmukh", "+91 91234 88990", "assets/image/room_studio.png", "2"));
                    roomDAO.save(new RoomItem("r5", "1 BHK Flat", "Baner, Pune", "₹ 13,000 / month", "4.1 km from you", "2 Occupants", "Rooms & PG", new String[]{"Furnished", "Wi-Fi", "Balcony", "Parking"}, "Cozy 1 BHK flat with living room sofa, coffee table, light wooden floor and natural light.", "Sneha Kulkarni", "+91 98765 43210", "assets/image/room_flat.png", "2"));

                    // Furniture Rentals
                    roomDAO.save(new RoomItem("r6", "Rent Study Table & Chair Set", "Kothrud, Pune", "₹ 299 / month", "1.8 km from you", "Student Rental", "Furniture", new String[]{"Free Delivery", "Flexible Tenure", "Maintenance Included"}, "Ergonomic wooden study desk with comfortable mesh chair for monthly student rental.", "EasyRent Campus", "+91 91122 33445", "assets/image/table_study.png", "2"));
                    roomDAO.save(new RoomItem("r7", "Rent Ergonomic Office Mesh Chair", "Baner, Pune", "₹ 199 / month", "2.1 km from you", "Student Rental", "Furniture", new String[]{"Adjustable Height", "Lumbar Support", "Clean"}, "High quality breathable mesh office chair for comfortable long study hours.", "Campus Furniture Rental", "+91 91133 44556", "assets/image/chair_office.png", "2"));
                    roomDAO.save(new RoomItem("r8", "Rent Foldable Metal Bed & Mattress", "Hinjewadi, Pune", "₹ 399 / month", "3.0 km from you", "Student Rental", "Furniture", new String[]{"Cotton Mattress", "Heavy Duty Steel", "Portable"}, "Sturdy single foldable bed frame with comfortable foam mattress included.", "Hostel Comforts", "+91 92244 55667", "assets/image/bed_foldable.png", "2"));

                    // Electronics Rentals
                    roomDAO.save(new RoomItem("r10", "Rent MacBook Air M1 Laptop", "Hinjewadi, Pune", "₹ 1,200 / day", "8.7 km from you", "Student Rental", "Electronics", new String[]{"8GB RAM", "256GB SSD", "M1 Chip"}, "Apple MacBook Air M1 available for project work, coding assignments & editing.", "TechRent Pune", "+91 95566 77889", "assets/image/laptop_macbook.png", "2"));
                    roomDAO.save(new RoomItem("r11", "Rent Dell i5 College Laptop", "Kothrud, Pune", "₹ 799 / month", "2.0 km from you", "Student Rental", "Electronics", new String[]{"Core i5", "8GB RAM", "Pre-loaded OS"}, "Reliable Dell Intel Core i5 laptop for online classes, assignments and exams.", "LaptopOnRent Pune", "+91 96677 88990", "assets/image/laptop_dell.png", "2"));

                    // Vehicle Rental
                    roomDAO.save(new RoomItem("r18", "Yamaha R15 V4 Bike", "Aundh, Pune", "₹ 450 / day", "2.8 km from you", "Vehicle Rental", "Vehicle", new String[]{"Helmet Included", "Low Mileage", "Serviced"}, "Sporty Yamaha R15 bike for daily campus commuting or weekend road trips.", "RideRent Pune", "+91 98888 77766", "assets/image/bike_yamaha.png", "2"));
                }

                // 3. Products
                ProductDAO productDAO = new ProductDAOImpl();
                List<ProductItem> products = productDAO.findAll();
                if (products.isEmpty()) {
                    System.out.println("[FirebaseSeedService] Seeding initial products...");
                    productDAO.save(new ProductItem("p1", "Higher Engineering Mathematics - B.S. Grewal", "₹ 450", "COEP Campus, Pune", "Book", "Used - Good", "Standard textbook for engineering mathematics. Clean pages without markings.", "Rahul Sharma", "+91 98765 11223", "assets/image/book_math.png", "2 hours ago", "3"));
                    productDAO.save(new ProductItem("p2", "Casio FX-991EX ClassWiz Calculator", "₹ 799", "Kothrud, Pune", "Electronics", "Like New", "Original Casio FX-991EX scientific calculator. Works perfectly with solar power backup.", "Priya Verma", "+91 98765 22334", "assets/image/calculator.png", "5 hours ago", "3"));
                    productDAO.save(new ProductItem("p3", "Wooden Study Desk with Drawer", "₹ 1,200", "Baner, Pune", "Furniture", "Used - Good", "Sturdy wooden table suitable for student study room. Light scratches on leg.", "Amit Kumar", "+91 98765 33445", "assets/image/table_study.png", "1 day ago", "3"));
                    productDAO.save(new ProductItem("p4", "Concept of Physics (Vol 1 & 2) - H.C. Verma", "₹ 350", "Viman Nagar, Pune", "Book", "Like New", "Both volumes of HC Verma physics in excellent condition. Ideal for competitive exams.", "Sneha Patel", "+91 98765 44556", "assets/image/book_physics.png", "1 day ago", "3"));
                    productDAO.save(new ProductItem("p5", "Fastrack Reflex Smart Watch", "₹ 999", "Hinjewadi, Pune", "Electronics", "Like New", "Smart watch with fitness tracking, heart rate monitor and long battery life. Includes box & charger.", "Rohan Deshmukh", "+91 98765 55667", "assets/image/watch_smart.png", "2 days ago", "3"));
                }

                // 4. Services
                ServiceDAO serviceDAO = new ServiceDAOImpl();
                List<ServiceItem> services = serviceDAO.findAll();
                if (services.isEmpty()) {
                    System.out.println("[FirebaseSeedService] Seeding initial services...");
                    serviceDAO.save(new ServiceItem("s1", "🧺", "Express Hostel Laundry", "Laundry", "Doorstep pickup & delivery", "₹ 15 / kg", "Quick Cleaners", "+91 98765 11111", "Complete laundry solution for college students. Eco-friendly detergent, stain removal.", "4"));
                    serviceDAO.save(new ServiceItem("s2", "🍲", "Annapurna Home Tiffin Service", "Mess & Tiffin", "Hygienic home-cooked meals", "₹ 2,500 / month", "Annapurna Tiffins", "+91 98765 22222", "Unlimited chapatis, 2 vegetable curries, dal, rice, salad, and Sunday special sweet.", "4"));
                    serviceDAO.save(new ServiceItem("s3", "🧹", "Daily Room & PG Cleaning", "Room Cleaning", "Professional deep cleaning", "₹ 499 / clean", "CleanHome Pros", "+91 98765 33333", "Floor scrubbing, bathroom sanitation, dusting, window glass cleaning and trash disposal.", "4"));
                    serviceDAO.save(new ServiceItem("s4", "🛵", "Fast 2-Wheeler Repair & Servicing", "Bike Service", "Doorstep bike repair", "₹ 199 + parts", "Speedy Bike Care", "+91 98765 44444", "On-demand mobile mechanic service for all two-wheelers. Emergency breakdown support.", "4"));
                }

                // 5. Roommates - dynamically registered by students, no fake/hardcoded seed data
                System.out.println("[FirebaseSeedService] Seed check completed successfully!");
            } catch (Throwable e) {
                System.err.println("[FirebaseSeedService] Seed check exception: " + e.getMessage());
            }
        }).start();
    }
}
