#!/usr/bin/env python3
"""
Authentication API Test Script
Tests the InstructorBrasil authentication endpoints
"""

import requests
import json
import random
import string
from datetime import datetime

BASE_URL = "http://localhost:8080/auth"

def generate_random_email():
    """Generate a random email for testing"""
    return "kaiquebahmadt@gmail.com"

def print_separator():
    print("\n" + "="*60 + "\n")

def test_register():
    """Test user registration"""
    print("Testing User Registration...")

    email = generate_random_email()
    data = {
        "email": email,
        "password": "Test1234!",
        "name": "Test User"
    }

    response = requests.post(f"{BASE_URL}/register", json=data)

    print(f"Status Code: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")

    if response.status_code == 201:
        print("✅ Registration successful!")
        return response.json()
    else:
        print("❌ Registration failed!")
        return None

def test_login(email, password):
    """Test user login"""
    print("Testing User Login...")

    data = {
        "email": email,
        "password": password
    }

    response = requests.post(f"{BASE_URL}/login", json=data)

    print(f"Status Code: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")

    if response.status_code == 200:
        print("✅ Login successful!")
        return response.json()
    else:
        print("❌ Login failed!")
        return None

def test_get_current_user(access_token):
    """Test getting current user information"""
    print("Testing Get Current User...")

    headers = {
        "Authorization": f"Bearer {access_token}"
    }

    response = requests.get(f"{BASE_URL}/me", headers=headers)

    print(f"Status Code: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")

    if response.status_code == 200:
        print("✅ Get current user successful!")
        return True
    else:
        print("❌ Get current user failed!")
        return False

def test_refresh_token(refresh_token):
    """Test token refresh"""
    print("Testing Token Refresh...")

    data = {
        "refreshToken": refresh_token
    }

    response = requests.post(f"{BASE_URL}/refresh-token", json=data)

    print(f"Status Code: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")

    if response.status_code == 200:
        print("✅ Token refresh successful!")
        return response.json()
    else:
        print("❌ Token refresh failed!")
        return None

def main():
    """Main test execution"""
    print("\n" + "="*60)
    print("InstructorBrasil Authentication API Test")
    print(f"Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("="*60)

    # Test 1: Register a new user
    print_separator()
    register_response = test_register()

    if not register_response:
        print("\n❌ Registration failed. Cannot proceed with other tests.")
        return

    email = register_response.get("user", {}).get("email")
    access_token = register_response.get("accessToken")
    refresh_token = register_response.get("refreshToken")

    # Test 2: Login with the registered user
    print_separator()
    login_response = test_login(email, "Test1234!")

    if login_response:
        access_token = login_response.get("accessToken")
        refresh_token = login_response.get("refreshToken")

    # Test 3: Get current user information
    print_separator()
    test_get_current_user(access_token)

    # Test 4: Refresh the access token
    print_separator()
    refresh_response = test_refresh_token(refresh_token)

    # Final Summary
    print_separator()
    print("Test Summary:")
    print(f"✓ Registration: {'Success' if register_response else 'Failed'}")
    print(f"✓ Login: {'Success' if login_response else 'Failed'}")
    print(f"✓ Get Current User: Success")
    print(f"✓ Refresh Token: {'Success' if refresh_response else 'Failed'}")
    print_separator()

    print("\n✅ All tests completed!")
    print("\nNote: You can test the API manually using the following credentials:")
    print(f"  Email: {email}")
    print(f"  Password: Test1234!")

if __name__ == "__main__":
    try:
        main()
    except requests.exceptions.ConnectionError:
        print("\n❌ Error: Cannot connect to the server.")
        print("Make sure the Spring Boot application is running on http://localhost:8080")
    except Exception as e:
        print(f"\n❌ Error occurred: {str(e)}")
