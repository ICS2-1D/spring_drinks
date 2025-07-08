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
import java.util.stream.Collectors;

@Component
public class BranchManager {
    // Key: IP Address (String), Value: Assigned Branch
    private final Map<String, Branch> ipToBranchMap = new ConcurrentHashMap<>();
    private final Set<Branch> assignedBranches = new ConcurrentSkipListSet<>();
    private final Object lock = new Object();

    @Value("${server.admin.ip:}")
    private String adminIpAddress;

    public Branch assignBranch(String clientId, InetAddress clientAddress, boolean isAdmin) throws IOException {
        synchronized (lock) {
            String clientIp = clientAddress.getHostAddress();
            System.out.println("🔍 Request to assign branch for IP: " + clientIp + " (Client ID: " + clientId + ", isAdmin: " + isAdmin + ")");

            // Step 1: Check if this IP address already has an assigned branch.
            if (ipToBranchMap.containsKey(clientIp)) {
                Branch existingBranch = ipToBranchMap.get(clientIp);
                System.out.println("✅ IP " + clientIp + " already has branch " + existingBranch.name() + ". Re-assigning.");
                return existingBranch;
            }

            // Step 2: If no existing assignment, find a new available branch.
            System.out.println("🤔 IP " + clientIp + " is new. Finding an available branch...");
            Branch newBranch = findAvailableBranch(clientIp, isAdmin);

            if (newBranch == null) {
                System.out.println("❌ No available branches. Currently assigned: " + assignedBranches);
                printCurrentAssignments();
                throw new IOException("No available branches. All " + Branch.values().length + " branches are occupied.");
            }

            // Step 3: Store the new assignment.
            ipToBranchMap.put(clientIp, newBranch);
            assignedBranches.add(newBranch);

            System.out.println("🏢 Assigned " + newBranch.name() + " to IP " + clientIp);
            System.out.println("📊 Branches assigned: " + assignedBranches.size() + "/" + Branch.values().length);
            printCurrentAssignments();

            return newBranch;
        }
    }

    /**
     * Unassigns a branch based on the client's IP address.
     * This is primarily for TCP clients that have a clear disconnect event.
     * @param clientAddress The address of the disconnecting client.
     */
    public void unassignBranch(InetAddress clientAddress) {
        synchronized (lock) {
            if (clientAddress == null) return;
            String clientIp = clientAddress.getHostAddress();

            if (ipToBranchMap.containsKey(clientIp)) {
                Branch branchToUnassign = ipToBranchMap.get(clientIp);

                ipToBranchMap.remove(clientIp);
                assignedBranches.remove(branchToUnassign);

                System.out.println("🔓 Unassigned " + branchToUnassign.name() + " from IP " + clientIp);
                System.out.println("📊 Branches assigned: " + assignedBranches.size() + "/" + Branch.values().length);
                printCurrentAssignments();
            } else {
                System.out.println("⚠️ Could not unassign branch for IP " + clientIp + ": No assignment found.");
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

    public Map<String, Branch> getIpToBranchMap() {
        return new ConcurrentHashMap<>(ipToBranchMap);
    }

    public Set<Branch> getAssignedBranches() {
        return new ConcurrentSkipListSet<>(assignedBranches);
    }

    public void printCurrentAssignments() {
        System.out.println("=== Current Branch Assignments by IP ===");
        if (ipToBranchMap.isEmpty()) {
            System.out.println("No clients currently have an assigned branch.");
        } else {
            ipToBranchMap.forEach((ip, branch) ->
                    System.out.println("IP: " + ip + " -> Branch: " + branch.name()));
        }
        System.out.println("Total assigned branches set: " + assignedBranches);
        System.out.println("Available branches: " + getAvailableBranches());
        System.out.println("==========================================");
    }

    private Set<Branch> getAvailableBranches() {
        Set<Branch> allBranches = Set.of(Branch.values());
        return allBranches.stream()
                .filter(branch -> !assignedBranches.contains(branch))
                .collect(Collectors.toSet());
    }
}
