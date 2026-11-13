import base64
import json

# Synthetic JWT from Lab 11 Task A (unsigned illustrative sample)
header = {"alg": "RS256", "typ": "JWT", "kid": "training-key-01"}
payload = {
    "iss": "https://identity.example.edu",
    "sub": "user-1042",
    "aud": "https://api.example.edu",
    "iat": 1794301200,
    "exp": 1794304800,
    "scope": "invoices:read profile",
    "roles": ["finance-analyst"],
    "department": "finance",
}

def b64url_encode(obj: dict) -> str:
    raw = json.dumps(obj, separators=(",", ":")).encode("utf-8")
    return base64.urlsafe_b64encode(raw).rstrip(b"=").decode("ascii")

def b64url_decode(segment: str) -> dict:
    padded = segment + "=" * (-len(segment) % 4)
    return json.loads(base64.urlsafe_b64decode(padded).decode("utf-8"))

sample_unsigned_jwt = f"{b64url_encode(header)}.{b64url_encode(payload)}."
print("=== Task A: Synthetic Unsigned JWT Inspection ===")
print("Encoded (unsigned):", sample_unsigned_jwt)
parts = sample_unsigned_jwt.split(".")
print("Decoded Header :", b64url_decode(parts[0]))
print("Decoded Payload:", b64url_decode(parts[1]))
print("Signature Part :", repr(parts[2]), "(empty -> MUST NOT be accepted)")

# Task D: RBAC Policy Evaluator
RBAC_PERMISSIONS = {
    "viewer": {"invoices:read"},
    "finance-analyst": {"invoices:read", "reports:read"},
    "billing-admin": {"invoices:read", "invoices:update", "users:manage"},
}

def check_rbac(role: str, action: str) -> str:
    return "ALLOW" if action in RBAC_PERMISSIONS.get(role, set()) else "DENY"

print("\n=== Task D: RBAC Policy Exercise ===")
rbac_cases = [
    ("Alex", "viewer", "invoices:read"),
    ("Alex", "viewer", "invoices:update"),
    ("Sam", "finance-analyst", "reports:read"),
    ("Riley", "billing-admin", "users:manage"),
]
for user, role, action in rbac_cases:
    print(f"{user:6s} | {role:15s} | {action:15s} -> {check_rbac(role, action)}")

# Task E: ABAC Policy Evaluator
def check_abac(user_dept: str, res_dept: str, classification: str, managed_device: bool) -> tuple[str, str]:
    if user_dept != res_dept:
        return "DENY", "subject.department != resource.department"
    if classification not in ("internal", "public"):
        return "DENY", "resource.classification is confidential"
    if not managed_device:
        return "DENY", "environment.device_managed is false"
    return "ALLOW", "All ABAC conditions satisfied"

print("\n=== Task E: ABAC Policy Exercise ===")
abac_cases = [
    (1, "finance", "finance", "internal", True),
    (2, "sales", "finance", "internal", True),
    (3, "finance", "finance", "confidential", True),
    (4, "finance", "finance", "public", False),
]
for case_id, u_dept, r_dept, cls, dev in abac_cases:
    decision, reason = check_abac(u_dept, r_dept, cls, dev)
    print(f"Case {case_id}: {decision:5s} ({reason})")
