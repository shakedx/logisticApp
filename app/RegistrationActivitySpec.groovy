import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import spock.lang.Specification
import static org.mockito.Mockito.*

class RegistrationActivitySpec extends Specification {

    def auth = Mock(FirebaseAuth)
    def database = Mock(FirebaseDatabase)
    def databaseReference = Mock(DatabaseReference)
    def user = Mock(FirebaseUser)
    def task = Mock(Task)
    def editTextLoginEmail = Mock(EditText)
    def editTextLoginPassword = Mock(EditText)
    def editTextFirstName = Mock(EditText)
    def editTextLastName = Mock(EditText)
    def context = Mock(Context)
    def activity = new RegistrationActivity()

    def setup() {
        activity.auth = auth
        activity.editTextLoginEmail = editTextLoginEmail
        activity.editTextLoginPassword = editTextLoginPassword
        activity.editTextFirstName = editTextFirstName
        activity.editTextLastName = editTextLastName
        activity.context = context

        task.isSuccessful >> true
        task.result >> Mock(AuthResult) {
            user >> user
        }
        user.uid >> "mockUid"
        database.reference >> databaseReference
        databaseReference.child(_) >> databaseReference
    }

    def "test successful registration"() {
        when:
        activity.registration("test@example.com", "password", "John", "Doe", "user")

        then:
        1 * auth.createUserWithEmailAndPassword("test@example.com", "password") >> task
        1 * databaseReference.child("users")
        1 * databaseReference.child("mockUid")
        1 * databaseReference.setValue(_) >> { Map<String, Object> userMap ->
            assert userMap["firstname"] == "John"
            assert userMap["lastname"] == "Doe"
            assert userMap["role"] == "user"
        }
    }
}