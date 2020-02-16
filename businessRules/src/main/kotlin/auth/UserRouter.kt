package auth

import generics.GenericRouter

class UserRouter : GenericRouter<User, UsersTable>(UsersService())