package com.ics.spring_drinks;

import com.ics.models.Branch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
public class BranchManager {
    private final Map<String, Branch> clientBranches = new ConcurrentHashMap<>();
    private final Set<Branch> assignedBranches = new ConcurrentSkipListSet<>();
    private final Object lock = new Object();

    @Value("${server.admin.ip:}")
    private String adminIpAddress;

    public Branch assignBranch(String clientId, InetAddress clientAddress, boolean isAdmin) throws IOException {
        synchronized (lock) {
            String clientIp = clientAddress.getHostAddress();
            System.out.println("🔍 Assigning branch for client IP: " + clientIp + ", isAdmin: " + isAdmin);

            Branch branch = findAvailableBranch(clientIp, isAdmin);

            if (branch == null) {
                System.out.println("❌ No available branches. Currently assigned: " + assignedBranches);
                printCurrentAssignments();
                throw new IOException("No available branches. All " + Branch.values().length + " branches are occupied.");
            }

            clientBranches.put(clientId, branch);
            assignedBranches.add(branch);

            System.out.println("🏢 Assigned " + branch.name() + " to client " + clientId + " (IP: " + clientIp + ")");
            System.out.println("📊 Branches assigned: " + assignedBranches.size() + "/" + Branch.values().length);

            return branch;
        }
    }

    public void unassignBranch(String clientId, Branch branch) {
        synchronized (lock) {
            if (branch != null && branch.equals(clientBranches.get(clientId))) {
                clientBranches.remove(clientId);
                assignedBranches.remove(branch);
                System.out.println("🔓 Unassigned " + branch.name() + " from client " + clientId);
                System.out.println("📊 Branches assigned: " + assignedBranches.size() + "/" + Branch.values().length);
            }
        }
    }

    private Branch findAvailableBranch(String clientIp, boolean isAdmin) {
        // Check if this IP should get NAIROBI automatically
        boolean shouldGetNairobi = shouldAssignNairobi(clientIp, isAdmin);

        if (shouldGetNairobi && !assignedBranches.contains(Branch.NAIROBI)) {
            System.out.println("🎯 Assigning NAIROBI to privileged client: " + clientIp);
            return Branch.NAIROBI;
        }

        // For regular clients, assign other branches in order
        for (Branch branch : Branch.values()) {
            if (branch != Branch.NAIROBI && !assignedBranches.contains(branch)) {
                System.out.println("🏪 Assigning " + branch.name() + " to client: " + clientIp);
                return branch;
            }
        }

        // If only NAIROBI is available but client shouldn't get it
        if (!assignedBranches.contains(Branch.NAIROBI)) {
            if (shouldGetNairobi) {
                System.out.println("🎯 Last resort: Assigning NAIROBI to privileged client: " + clientIp);
                return Branch.NAIROBI;
            } else {
                System.out.println("⚠️ Only NAIROBI available but client is not privileged: " + clientIp);
            }
        }

        return null;
    }

    private boolean shouldAssignNairobi(String clientIp, boolean isAdmin) {
        // Your PC's IP always gets NAIROBI
        if (adminIpAddress != null && !adminIpAddress.isEmpty() && clientIp.equals(adminIpAddress)) {
            return true;
        }

        // Admin connections get NAIROBI
        if (isAdmin) {
            return true;
        }

        // Localhost connections get NAIROBI (for development)
        if (clientIp.equals("127.0.0.1") || clientIp.equals("0:0:0:0:0:0:0:1")) {
            return true;
        }

        return false;
    }

    public Map<String, Branch> getClientBranches() {
        return new ConcurrentHashMap<>(clientBranches);
    }

    public Set<Branch> getAssignedBranches() {
        return new ConcurrentSkipListSet<>(assignedBranches);
    }

    public void printCurrentAssignments() {
        System.out.println("=== Current Branch Assignments ===");
        if (clientBranches.isEmpty()) {
            System.out.println("No clients currently connected.");
        } else {
            clientBranches.forEach((clientId, branch) ->
                    System.out.println("Client: " + clientId + " -> Branch: " + branch.name()));
        }
        System.out.println("Assigned branches: " + assignedBranches);
        System.out.println("Available branches: " + getAvailableBranches());
        System.out.println("==============================");
    }

    private Set<Branch> getAvailableBranches() {
        Set<Branch> available = new ConcurrentSkipListSet<>();
        for (Branch branch : Branch.values()) {
            if (!assignedBranches.contains(branch)) {
                available.add(branch);
            }
        }
        return available;
    }
}