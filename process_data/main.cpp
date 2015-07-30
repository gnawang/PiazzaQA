#include <iostream>
#include "rapidxml_utils.hpp"

using namespace rapidxml;
using std::cout;

void explore_chidren(xml_node<> *parent){

  if(!parent) return;

  for (xml_node<> *i = parent->first_node();
       i; i = i->next_sibling() )
  {

    if( (!strcmp(i->name(), "content") ||
     !strcmp(i->name(), "subject") ) && strlen( i->value() )!=0 ) {
    //cout << i->name() <<"\n";
    cout << " <TEXT>\n" << i->value() << "</TEXT>\n";
  }
  //  for (xml_attribute<> *attr = i->first_attribute();
  //       attr; attr = attr->next_attribute())
  //  {
  //      cout << "With attributes " << attr->name() << ":";
  //      cout << attr->value();
  //  }
  //  cout<< "\n";


  explore_chidren( i );

  }



}

int main(){
    file<> xmlFile("clean_class_content.xml"); // Default template is char
    xml_document<> doc;
    doc.parse<0>(xmlFile.data());
    explore_chidren(&doc);

  //  xml_node<> *node = doc.first_node()->first_node();
  //  cout << "Name of my first node is: " << node->name() << "\n";
  //  cout << "Node foobar has value " << node->value() << "\n";
  //  for (xml_attribute<> *attr = node->first_attribute();
  //       attr; attr = attr->next_attribute())
  //  {
  //      cout << "Node foobar has attribute " << attr->name() << " ";
  //      cout << "with value " << attr->value() << "\n";
  //  }

  return 0;
}
