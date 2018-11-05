# Client writen in pyhton
import requests

if __name__ == "__main__":
    main()

def main():
    while True:
        user_input = raw_input(showOptions(0))

        # buy flight tickets
        if user_input == "1":
            user_input = raw_input(showOptions(1))
            if user_input == "0":
                continue
            # buyTicket()

        # book accomodation
        elif user_input == "2":
            user_input = raw_input(showOptions(2))
            if user_input == "0":
                continue
            # bookRoom()

        # buy package
        elif user_input == "3":
            user_input = raw_input(showOptions(3))
            if user_input == "0":
                continue
            # buyPackage()

# show options for client
def showOptions(option):
    cover = "\nWhat do you want to do? Press the number matching your choice.\n";
    options = "  1 - Buy flight.\n  2 - Book hotel.\n  3 - Buy package.\n";
    options1 = "\n**BUY FLIGHT**\nEnter the flight ID to buy. Or press 0 to go back.\n";
    options2 = "\n**BOOK HOTEL**\nEnter the accomodation ID to buy. Or press 0 to go back.\n";
    options3 = "\n**BUY PACKAGE**\nEnter the flight ID and the accomodation ID you want buy. Or press 0 to go back.\n";

    if option == 0:
        print(cover)
        print(options)
    elif option == 1:
        print(options1)
    elif option == 2:
        print(options2)
    elif option == 3:
        print(options3)
    else:
        print("Erro no menu\n")

# buy flight tickets
def buyTicket():
    r = requests.get('https://localhost:8080/agency/flights')
    r.json()

# book bookRoom
def bookRoom():
    r = requests.get('https://localhost:8080/agency/hotels')
    r.json()

# buy package
def buyPackage():
    r_tickets = requests.get('https://localhost:8080/agency/flights')
    r_hotels = requests.get('https://localhost:8080/agency/hotels')
    r_tickets.json()
    r_hotels.json()

# prints JSON archives organized
def printJson():
