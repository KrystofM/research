import numpy as np

# parameters
num_slots = 32
prob_outpatient_show = 0.84
prob_emergency = 0.1
prob_inpatient = 0.4
revenue_outpatient = 100
revenue_inpatient = 20
waiting_cost_outpatient = 1.5
penalty_outpatient = 10
penalty_inpatient = 200

# Initialize value function and policy
value_function = np.zeros((num_slots+1, num_slots+1, num_slots+1, num_slots+1))
policy = np.zeros((num_slots+1, num_slots+1, num_slots+1, num_slots+1))

# Iterate over slots in reverse order
for slot in range(num_slots, -1, -1):
    # Iterate over all possible states
    for num_outpatients in range(num_slots+1):
        for num_inpatients in range(num_slots - num_outpatients + 1):
            for num_emergencies in range(num_slots - num_outpatients - num_inpatients + 1):

                # If it's not the first slot
                if slot < num_slots:
                    # Calculate expected values for decision = 0 and decision = 1
                    # decision 0 leaves the slot open, decision 1 schedules an outpatient
                    
                    # Expected value when no outpatient is scheduled (decision = 0)
                    exp_val_0 = (prob_inpatient * (prob_emergency * (revenue_inpatient - waiting_cost_outpatient + value_function[slot+1, num_outpatients, min(num_inpatients+1, num_slots), min(num_emergencies+1, num_slots)]) 
                        + (1 - prob_emergency) * (revenue_inpatient - waiting_cost_outpatient + value_function[slot+1, num_outpatients, min(num_inpatients+1, num_slots), num_emergencies])) 
                        + (1 - prob_inpatient) * (prob_emergency * value_function[slot+1, num_outpatients, num_inpatients, min(num_emergencies+1, num_slots)] 
                        + (1 - prob_emergency) * value_function[slot+1, num_outpatients, num_inpatients, num_emergencies]))

                    # Expected value when an outpatient is scheduled (decision = 1)
                    exp_val_1 = (prob_outpatient_show * prob_inpatient * (prob_emergency * (revenue_outpatient - waiting_cost_outpatient + value_function[slot+1, max(num_outpatients-1, 0), min(num_inpatients+1, num_slots), min(num_emergencies+1, num_slots)]) 
                        + (1 - prob_emergency) * (revenue_outpatient - waiting_cost_outpatient + value_function[slot+1, max(num_outpatients-1, 0), min(num_inpatients+1, num_slots), num_emergencies]))
                        + prob_outpatient_show * (1 - prob_inpatient) * (prob_emergency * (revenue_outpatient - waiting_cost_outpatient + value_function[slot+1, max(num_outpatients-1, 0), num_inpatients, min(num_emergencies+1, num_slots)])
                        + (1 - prob_emergency) * (revenue_outpatient - waiting_cost_outpatient + value_function[slot+1, max(num_outpatients-1, 0), num_inpatients, num_emergencies]))
                        + (1 - prob_outpatient_show) * prob_inpatient * (prob_emergency * (-waiting_cost_outpatient + value_function[slot+1, num_outpatients, min(num_inpatients+1, num_slots), min(num_emergencies+1, num_slots)])
                        + (1 - prob_emergency) * (-waiting_cost_outpatient + value_function[slot+1, num_outpatients, min(num_inpatients+1, num_slots), num_emergencies]))
                        + (1 - prob_outpatient_show) * (1 - prob_inpatient) * (prob_emergency * (-waiting_cost_outpatient + value_function[slot+1, num_outpatients, num_inpatients, min(num_emergencies+1, num_slots)])
                        + (1 - prob_emergency) * (-waiting_cost_outpatient + value_function[slot+1, num_outpatients, num_inpatients, num_emergencies]))) * (num_outpatients > 0)
                    
                    # Choose the decision with the higher expected value
                    if exp_val_0 > exp_val_1:
                        value_function[slot, num_outpatients, num_inpatients, num_emergencies] = exp_val_0
                        policy[slot, num_outpatients, num_inpatients, num_emergencies] = 0
                    else:
                        value_function[slot, num_outpatients, num_inpatients, num_emergencies] = exp_val_1
                        policy[slot, num_outpatients, num_inpatients, num_emergencies] = 1
                
                # If it's the first slot
                else:
                    # Calculate penalties
                    value_function[slot, num_outpatients, num_inpatients, num_emergencies] = -penalty_outpatient * num_outpatients - penalty_inpatient * num_inpatients

# for slot in range(num_slots, -1, -1):
#     # Iterate over all possible states
#     for num_outpatients in range(num_slots+1):
#         for num_inpatients in range(num_slots - num_outpatients + 1):
#             for num_emergencies in range(num_slots - num_outpatients - num_inpatients + 1):
#                 print(f"The optimal policy for slot {slot}, with {num_outpatients} outpatients and {num_inpatients} inpatients and {num_emergencies} emergencies is: {policy[slot, num_outpatients, num_inpatients, num_emergencies]}")

print("The expected profit is:", value_function[0,0,0,0])
