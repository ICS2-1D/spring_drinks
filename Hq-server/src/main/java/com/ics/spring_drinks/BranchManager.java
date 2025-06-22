package com.ics.spring_drinks;

import com.ics.models.Branch;
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

    public Branch assignBranch(String clientId, InetAddress clientAddress) throws IOException {
        synchronized (lock) {
            Branch branch = findAvailableBranch(clientAddress);

            if (branch == null) {
                throw new IOException("No available branches. All " + Branch.values().length + " branches occupied.");
            }

            clientBranches.put(clientId, branch);
            assignedBranches.add(branch);

            System.out.println("🏢 Assigned " + branch.name() + " to client " + clientId);
            System.out.println("📊 Branches assigned: " + assignedBranches.size() + "/" + Branch.values().length);

            return branch;
        }
    }

    public void unassignBranch(String clientId, Branch branch) {
        synchronized (lock) {
            clientBranches.remove(clientId);
            assignedBranches.remove(branch);
            System.out.println("🔓 Unassigned " + branch.name() + " from client " + clientId);
        }
    }

    private Branch findAvailableBranch(InetAddress clientAddress) {
        // Admin branch (Nairobi) for local connections
        if (isLocalConnection(clientAddress) && !assignedBranches.contains(Branch.NAIROBI)) {
            return Branch.NAIROBI;
        }

        // Find any other available branch
        for (Branch branch : Branch.values()) {
            if (!assignedBranches.contains(branch) && branch != Branch.NAIROBI) {
                return branch;
            }
        }

        return null; // No branches available
    }

    private boolean isLocalConnection(InetAddress address) {
        return address.isLoopbackAddress() || address.isAnyLocalAddress();
    }

    // Getters for monitoring
    public Map<String, Branch> getClientBranches() {
        return new ConcurrentHashMap<>(clientBranches);
    }

    public Set<Branch> getAssignedBranches() {
        return new ConcurrentSkipListSet<>(assignedBranches);
    }
}
