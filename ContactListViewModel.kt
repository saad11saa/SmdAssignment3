import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ContactListViewModel(private val dispatcher: CoroutineContext = Dispatchers.IO) : ViewModel() {

    private val databaseHelper = DatabaseHelper(ApplicationProvider.getApplicationContext())
    private val _contactList = MutableLiveData<List<Contact>>(emptyList())
    val contactList: LiveData<List<Contact>> = _contactList

    init {
        loadContacts()
    }

    fun importContacts() {
        launch(dispatcher) {
            val contentResolver: ContentResolver = ApplicationProvider.getApplicationContext().contentResolver
            val cursor: Cursor? = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            val importedContacts = mutableListOf<Contact>()
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val name = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    )
                    val phoneNumber = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    )

                    val contact = Contact(name, phoneNumber)
                    databaseHelper.insertContact(contact)
                    importedContacts.add(contact)
                } while (cursor.moveToNext())
            }
            cursor?.close()

            // Update contact list with imported contacts
            _contactList.postValue(_contactList.value!! + importedContacts)
        }
    }

    fun loadContacts() {
        launch(dispatcher) {
            val contacts = databaseHelper.getAllContacts()
            _contactList.postValue(contacts)
        }
    }

    fun onContactClick(contact: Contact) {
        // Open contact details s
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("tel:${contact.phoneNumber}")
        ApplicationProvider.getApplicationContext().startActivity(intent)
    }

    fun editContact(contact: Contact) {

        launch(dispatcher) {
            databaseHelper.updateContact(contact)
            loadContacts()
        }
    }

    fun deleteContact(contact: Contact) {

        launch(dispatcher) {
            databaseHelper.deleteContact(contact.id)
            loadContacts()
        }
    }

    fun callContact(contact: Contact) {
        // Open phone dialer phone number
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${contact.phoneNumber}")
        }
        ApplicationProvider.getApplicationContext
}
